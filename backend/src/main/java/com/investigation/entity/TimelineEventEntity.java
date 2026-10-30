package com.investigation.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "timeline_events")
public class TimelineEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    @JsonIgnore
    private CaseEntity caseEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evidence_id")
    @JsonIgnore
    private EvidenceEntity evidenceEntity;

    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "source_reference", length = 255)
    private String sourceReference;

    @Column(name = "is_anomaly", nullable = false)
    private boolean isAnomaly = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public TimelineEventEntity() {}

    public TimelineEventEntity(CaseEntity caseEntity, EvidenceEntity evidenceEntity, Instant eventTimestamp, String title, String description, String eventType, String sourceReference, boolean isAnomaly) {
        this.caseEntity = caseEntity;
        this.evidenceEntity = evidenceEntity;
        this.eventTimestamp = eventTimestamp;
        this.title = title;
        this.description = description;
        this.eventType = eventType;
        this.sourceReference = sourceReference;
        this.isAnomaly = isAnomaly;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public CaseEntity getCaseEntity() { return caseEntity; }
    public void setCaseEntity(CaseEntity caseEntity) { this.caseEntity = caseEntity; }

    public EvidenceEntity getEvidenceEntity() { return evidenceEntity; }
    public void setEvidenceEntity(EvidenceEntity evidenceEntity) { this.evidenceEntity = evidenceEntity; }

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

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
