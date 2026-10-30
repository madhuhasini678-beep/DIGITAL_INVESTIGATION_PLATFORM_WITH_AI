package com.investigation.dto;

import com.investigation.entity.TimelineEventEntity;
import java.time.Instant;
import java.util.UUID;

public class TimelineEventResponse {

    private UUID id;
    private UUID caseId;
    private UUID evidenceId;
    private Instant eventTimestamp;
    private String title;
    private String description;
    private String eventType;
    private String sourceReference;
    private boolean isAnomaly;

    public TimelineEventResponse() {}

    public static TimelineEventResponse fromEntity(TimelineEventEntity entity) {
        TimelineEventResponse resp = new TimelineEventResponse();
        resp.setId(entity.getId());
        resp.setCaseId(entity.getCaseEntity().getId());
        resp.setEvidenceId(entity.getEvidenceEntity() != null ? entity.getEvidenceEntity().getId() : null);
        resp.setEventTimestamp(entity.getEventTimestamp());
        resp.setTitle(entity.getTitle());
        resp.setDescription(entity.getDescription());
        resp.setEventType(entity.getEventType());
        resp.setSourceReference(entity.getSourceReference());
        resp.setAnomaly(entity.isAnomaly());
        return resp;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCaseId() { return caseId; }
    public void setCaseId(UUID caseId) { this.caseId = caseId; }

    public UUID getEvidenceId() { return evidenceId; }
    public void setEvidenceId(UUID evidenceId) { this.evidenceId = evidenceId; }

    public Instant getEventTimestamp() { return eventTimestamp; }
    public void setEventTimestamp(Instant eventTimestamp) { this.eventTimestamp = eventTimestamp; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getSourceReference() { return sourceReference; }
    public void setSourceReference(String sourceReference) { this.sourceReference = sourceReference; }

    public boolean isAnomaly() { return isAnomaly; }
    public void setAnomaly(boolean anomaly) { isAnomaly = anomaly; }
}
