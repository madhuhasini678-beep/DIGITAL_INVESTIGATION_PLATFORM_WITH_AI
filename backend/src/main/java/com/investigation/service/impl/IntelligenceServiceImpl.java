package com.investigation.service.impl;

import com.investigation.client.AiServiceGateway;
import com.investigation.dto.AiSummaryResponse;
import com.investigation.dto.AnomalyResponse;
import com.investigation.entity.*;
import com.investigation.parser.CsvEvidenceParser;
import com.investigation.parser.EvidenceRow;
import com.investigation.parser.ParsedEvidenceResult;
import com.investigation.repository.*;
import com.investigation.service.IntelligenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class IntelligenceServiceImpl implements IntelligenceService {

    private final CaseRepository caseRepository;
    private final EvidenceRepository evidenceRepository;
    private final TimelineEventRepository timelineEventRepository;
    private final GraphNodeRepository graphNodeRepository;
    private final GraphEdgeRepository graphEdgeRepository;
    private final AnomalyRepository anomalyRepository;
    private final AiSummaryRepository aiSummaryRepository;
    private final CsvEvidenceParser csvEvidenceParser;
    private final AiServiceGateway aiServiceGateway;

    public IntelligenceServiceImpl(
            CaseRepository caseRepository,
            EvidenceRepository evidenceRepository,
            TimelineEventRepository timelineEventRepository,
            GraphNodeRepository graphNodeRepository,
            GraphEdgeRepository graphEdgeRepository,
            AnomalyRepository anomalyRepository,
            AiSummaryRepository aiSummaryRepository,
            CsvEvidenceParser csvEvidenceParser,
            AiServiceGateway aiServiceGateway) {
        this.caseRepository = caseRepository;
        this.evidenceRepository = evidenceRepository;
        this.timelineEventRepository = timelineEventRepository;
        this.graphNodeRepository = graphNodeRepository;
        this.graphEdgeRepository = graphEdgeRepository;
        this.anomalyRepository = anomalyRepository;
        this.aiSummaryRepository = aiSummaryRepository;
        this.csvEvidenceParser = csvEvidenceParser;
        this.aiServiceGateway = aiServiceGateway;
    }

    @Override
    public List<AnomalyResponse> detectAndSaveAnomalies(UUID caseId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new NoSuchElementException("Case not found with ID: " + caseId));

        List<EvidenceEntity> evidenceList = evidenceRepository.findByCaseEntityId(caseId);
        List<Map<String, Object>> recordsForAi = new ArrayList<>();

        for (EvidenceEntity ev : evidenceList) {
            try {
                Path p = Paths.get(ev.getStoragePath());
                if (Files.exists(p)) {
                    try (InputStream is = Files.newInputStream(p)) {
                        ParsedEvidenceResult res = csvEvidenceParser.parse(is);
                        for (EvidenceRow row : res.getRows()) {
                            Map<String, Object> r = new HashMap<>();
                            r.put("record_id", ev.getFileName() + "-Row-" + row.getRowNumber());
                            if (row.getParsedTimestamp() != null) {
                                r.put("timestamp", row.getParsedTimestamp().toString());
                            }

                            // Extract numerical value
                            Double numVal = extractNumericalValue(row.getData());
                            if (numVal != null) {
                                r.put("numerical_value", numVal);
                            }

                            r.put("raw_attributes", row.getData());
                            recordsForAi.add(r);
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        List<AiServiceGateway.RawAnomaly> rawAnomalies = aiServiceGateway.detectAnomalies(
                caseId.toString(),
                caseEntity.getTitle(),
                caseEntity.getCategory().name(),
                recordsForAi
        );

        // Delete old anomalies and replace with fresh scan
        List<AnomalyEntity> existing = anomalyRepository.findByCaseEntityIdOrderByDetectedAtDesc(caseId);
        if (!existing.isEmpty()) {
            anomalyRepository.deleteAll(existing);
        }

        List<AnomalyEntity> newEntities = new ArrayList<>();
        for (AiServiceGateway.RawAnomaly raw : rawAnomalies) {
            AnomalyEntity entity = new AnomalyEntity(
                    caseEntity,
                    raw.anomalyType,
                    raw.title,
                    raw.description,
                    raw.severity,
                    raw.confidenceScore,
                    String.join(",", raw.flaggedRecordIds)
            );
            newEntities.add(entity);
        }

        List<AnomalyEntity> saved = anomalyRepository.saveAll(newEntities);
        return saved.stream().map(AnomalyResponse::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnomalyResponse> getAnomalies(UUID caseId) {
        return anomalyRepository.findByCaseEntityIdOrderByDetectedAtDesc(caseId).stream()
                .map(AnomalyResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public AiSummaryResponse generateAndSaveSummary(UUID caseId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new NoSuchElementException("Case not found with ID: " + caseId));

        int evidenceCount = (int) evidenceRepository.countByCaseEntityId(caseId);
        List<TimelineEventEntity> timelineEvents = timelineEventRepository.findByCaseEntityIdOrderByEventTimestampAsc(caseId);
        int nodeCount = (int) graphNodeRepository.countByCaseEntityId(caseId);
        int edgeCount = graphEdgeRepository.findByCaseEntityId(caseId).size();
        List<AnomalyEntity> anomalies = anomalyRepository.findByCaseEntityIdOrderByDetectedAtDesc(caseId);

        List<Map<String, Object>> timelineForAi = timelineEvents.stream().map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("timestamp", e.getEventTimestamp().toString());
            m.put("title", e.getTitle());
            m.put("description", e.getDescription());
            m.put("is_anomaly", e.isAnomaly());
            return m;
        }).collect(Collectors.toList());

        List<AiServiceGateway.RawAnomaly> rawAnomalies = anomalies.stream().map(a -> {
            AiServiceGateway.RawAnomaly ra = new AiServiceGateway.RawAnomaly();
            ra.anomalyType = a.getAnomalyType();
            ra.title = a.getTitle();
            ra.description = a.getDescription();
            ra.severity = a.getSeverity();
            ra.confidenceScore = a.getConfidenceScore();
            return ra;
        }).collect(Collectors.toList());

        AiServiceGateway.RawSummary rawSummary = aiServiceGateway.generateSummary(
                caseId.toString(),
                caseEntity.getTitle(),
                caseEntity.getCategory().name(),
                caseEntity.getDescription(),
                caseEntity.getIncidentDate() != null ? caseEntity.getIncidentDate().toString() : null,
                caseEntity.isHistoricalDemo(),
                evidenceCount,
                timelineForAi,
                nodeCount,
                edgeCount,
                rawAnomalies
        );

        AiSummaryEntity entity = aiSummaryRepository.findByCaseEntityId(caseId).orElseGet(() -> {
            AiSummaryEntity s = new AiSummaryEntity();
            s.setCaseEntity(caseEntity);
            return s;
        });

        entity.setFactualOverview(rawSummary.factualOverview);
        entity.setKeyFindings(rawSummary.keyFindings);
        entity.setDiscrepanciesIdentified(rawSummary.discrepanciesIdentified);
        entity.setConfidenceNotes(rawSummary.confidenceNotes);

        AiSummaryEntity saved = aiSummaryRepository.save(entity);
        return AiSummaryResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AiSummaryResponse getSummary(UUID caseId) {
        AiSummaryEntity entity = aiSummaryRepository.findByCaseEntityId(caseId)
                .orElseThrow(() -> new NoSuchElementException("AI summary not found for case ID: " + caseId));
        return AiSummaryResponse.fromEntity(entity);
    }

    private Double extractNumericalValue(Map<String, String> data) {
        for (String colKey : List.of("amount", "crores", "rupees", "usd", "value", "pressure", "temp", "temperature", "reading", "count")) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                if (entry.getKey().toLowerCase().contains(colKey)) {
                    try {
                        String clean = entry.getValue().replaceAll("[^0-9.-]", "");
                        if (!clean.isEmpty()) {
                            return Double.parseDouble(clean);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return null;
    }
}
