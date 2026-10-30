package com.investigation.service.impl;

import com.investigation.dto.EvidencePreviewResponse;
import com.investigation.dto.EvidenceResponse;
import com.investigation.dto.GraphPayloadResponse;
import com.investigation.dto.TimelineEventResponse;
import com.investigation.entity.*;
import com.investigation.parser.CsvEvidenceParser;
import com.investigation.parser.EvidenceRow;
import com.investigation.parser.ParsedEvidenceResult;
import com.investigation.repository.*;
import com.investigation.service.EvidenceService;
import com.investigation.service.GraphExtractionService;
import com.investigation.service.TimelineExtractionService;
import com.investigation.util.CryptoUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class EvidenceServiceImpl implements EvidenceService {

    private final CaseRepository caseRepository;
    private final EvidenceRepository evidenceRepository;
    private final TimelineEventRepository timelineEventRepository;
    private final GraphNodeRepository graphNodeRepository;
    private final GraphEdgeRepository graphEdgeRepository;
    private final CsvEvidenceParser csvEvidenceParser;
    private final TimelineExtractionService timelineExtractionService;
    private final GraphExtractionService graphExtractionService;

    private static final String UPLOAD_DIR = "data/uploads";

    public EvidenceServiceImpl(
            CaseRepository caseRepository,
            EvidenceRepository evidenceRepository,
            TimelineEventRepository timelineEventRepository,
            GraphNodeRepository graphNodeRepository,
            GraphEdgeRepository graphEdgeRepository,
            CsvEvidenceParser csvEvidenceParser,
            TimelineExtractionService timelineExtractionService,
            GraphExtractionService graphExtractionService) {
        this.caseRepository = caseRepository;
        this.evidenceRepository = evidenceRepository;
        this.timelineEventRepository = timelineEventRepository;
        this.graphNodeRepository = graphNodeRepository;
        this.graphEdgeRepository = graphEdgeRepository;
        this.csvEvidenceParser = csvEvidenceParser;
        this.timelineExtractionService = timelineExtractionService;
        this.graphExtractionService = graphExtractionService;
    }

    @Override
    public EvidenceResponse uploadEvidence(UUID caseId, MultipartFile file, boolean isSimulated) throws IOException {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new NoSuchElementException("Case not found with ID: " + caseId));

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file");
        }

        byte[] fileBytes = file.getBytes();
        String sha256 = CryptoUtil.computeSha256(fileBytes);

        // Save file locally in data/uploads/{caseId}/
        Path caseUploadDir = Paths.get(UPLOAD_DIR, caseId.toString());
        Files.createDirectories(caseUploadDir);

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "evidence.csv";
        String storedFileName = UUID.randomUUID().toString() + "_" + originalName;
        Path targetPath = caseUploadDir.resolve(storedFileName);
        Files.write(targetPath, fileBytes);

        // Parse CSV
        ParsedEvidenceResult parsedResult = csvEvidenceParser.parse(new ByteArrayInputStream(fileBytes));

        String joinedHeaders = String.join(",", parsedResult.getHeaders());
        String fileType = originalName.endsWith(".csv") ? "CSV" : "TEXT";

        EvidenceEntity evidence = new EvidenceEntity(
                caseEntity,
                originalName,
                fileType,
                file.getSize(),
                sha256,
                targetPath.toString(),
                joinedHeaders,
                parsedResult.getRowCount(),
                isSimulated
        );

        EvidenceEntity savedEvidence = evidenceRepository.save(evidence);

        // Automatically extract timeline events
        timelineExtractionService.extractAndPersistTimelineEvents(caseEntity, savedEvidence, parsedResult);

        // Automatically extract relationship graph
        graphExtractionService.extractAndPersistGraph(caseEntity, parsedResult);

        return EvidenceResponse.fromEntity(savedEvidence);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvidenceResponse> getEvidenceByCase(UUID caseId) {
        return evidenceRepository.findByCaseEntityId(caseId).stream()
                .map(EvidenceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EvidenceResponse getEvidenceById(UUID id) {
        EvidenceEntity entity = evidenceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Evidence not found with ID: " + id));
        return EvidenceResponse.fromEntity(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public EvidencePreviewResponse getEvidencePreview(UUID id, int page, int pageSize) throws IOException {
        EvidenceEntity entity = evidenceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Evidence not found with ID: " + id));

        Path filePath = Paths.get(entity.getStoragePath());
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("Evidence file not found on disk: " + entity.getStoragePath());
        }

        try (InputStream is = Files.newInputStream(filePath)) {
            ParsedEvidenceResult parsed = csvEvidenceParser.parse(is);
            List<EvidenceRow> allRows = parsed.getRows();

            int fromIndex = Math.min(page * pageSize, allRows.size());
            int toIndex = Math.min(fromIndex + pageSize, allRows.size());

            List<Map<String, String>> pageRows = allRows.subList(fromIndex, toIndex).stream()
                    .map(EvidenceRow::getData)
                    .collect(Collectors.toList());

            return new EvidencePreviewResponse(
                    id,
                    entity.getFileName(),
                    parsed.getHeaders(),
                    pageRows,
                    allRows.size(),
                    page,
                    pageSize
            );
        }
    }

    @Override
    public void deleteEvidence(UUID id) {
        EvidenceEntity entity = evidenceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Evidence not found with ID: " + id));

        // Unlink or clean up any timeline events referencing this evidence
        List<TimelineEventEntity> linkedEvents = timelineEventRepository.findByEvidenceEntityId(id);
        for (TimelineEventEntity event : linkedEvents) {
            event.setEvidenceEntity(null);
            timelineEventRepository.save(event);
        }

        // Delete physical file
        try {
            Path filePath = Paths.get(entity.getStoragePath());
            Files.deleteIfExists(filePath);
        } catch (Exception ignored) {}

        evidenceRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimelineEventResponse> getTimelineEvents(UUID caseId) {
        return timelineEventRepository.findByCaseEntityIdOrderByEventTimestampAsc(caseId).stream()
                .map(TimelineEventResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GraphPayloadResponse getGraphPayload(UUID caseId) {
        List<GraphNodeEntity> nodes = graphNodeRepository.findByCaseEntityId(caseId);
        List<GraphEdgeEntity> edges = graphEdgeRepository.findByCaseEntityId(caseId);
        return new GraphPayloadResponse(caseId, nodes, edges);
    }
}
