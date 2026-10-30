package com.investigation.service;

import com.investigation.entity.CaseEntity;
import com.investigation.entity.EvidenceEntity;
import com.investigation.entity.TimelineEventEntity;
import com.investigation.parser.EvidenceRow;
import com.investigation.parser.ParsedEvidenceResult;
import com.investigation.repository.TimelineEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TimelineExtractionService {

    private final TimelineEventRepository timelineEventRepository;

    public TimelineExtractionService(TimelineEventRepository timelineEventRepository) {
        this.timelineEventRepository = timelineEventRepository;
    }

    public List<TimelineEventEntity> extractAndPersistTimelineEvents(
            CaseEntity caseEntity,
            EvidenceEntity evidenceEntity,
            ParsedEvidenceResult parsedResult) {

        List<TimelineEventEntity> events = new ArrayList<>();
        Instant previousTimestamp = null;

        for (EvidenceRow row : parsedResult.getRows()) {
            Instant timestamp = row.getParsedTimestamp();
            if (timestamp == null) {
                continue;
            }

            String title = extractTitle(row.getData());
            String description = formatRowDescription(row.getData());
            String eventType = determineEventType(row.getData());
            String sourceRef = evidenceEntity.getFileName() + " (Row " + row.getRowNumber() + ")";

            // Check if chronological sequence is inverted
            boolean isAnomaly = previousTimestamp != null && timestamp.isBefore(previousTimestamp);
            previousTimestamp = timestamp;

            TimelineEventEntity event = new TimelineEventEntity(
                    caseEntity,
                    evidenceEntity,
                    timestamp,
                    title,
                    description,
                    eventType,
                    sourceRef,
                    isAnomaly
            );
            events.add(event);
        }

        if (!events.isEmpty()) {
            return timelineEventRepository.saveAll(events);
        }
        return events;
    }

    private String extractTitle(Map<String, String> data) {
        for (String key : List.of("title", "event", "action", "activity", "type", "transaction_type", "status", "operation")) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(key) && entry.getValue() != null && !entry.getValue().isBlank()) {
                    return entry.getValue().trim();
                }
            }
        }

        // Fallback: use first non-empty, non-id field
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String k = entry.getKey().toLowerCase();
            if (!k.contains("id") && !k.contains("date") && !k.contains("time") && entry.getValue() != null && !entry.getValue().isBlank()) {
                return entry.getKey() + ": " + entry.getValue();
            }
        }
        return "Forensic Event Record";
    }

    private String determineEventType(Map<String, String> data) {
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String k = entry.getKey().toLowerCase();
            if (k.contains("type") || k.contains("category")) {
                if (entry.getValue() != null && !entry.getValue().isBlank()) {
                    return entry.getValue().toUpperCase().replaceAll("[^A-Z0-9]", "_");
                }
            }
        }
        return "EVIDENCE_LOG";
    }

    private String formatRowDescription(Map<String, String> data) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (count > 0) sb.append(" | ");
            sb.append(entry.getKey()).append(": ").append(entry.getValue());
            count++;
            if (count >= 5) break; // Keep description concise
        }
        return sb.toString();
    }
}
