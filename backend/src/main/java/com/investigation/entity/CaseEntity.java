package com.investigation.entity;

import com.investigation.entity.enums.CaseCategory;
import com.investigation.entity.enums.CaseStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cases")
public class CaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "case_number", nullable = false, unique = true, length = 50)
    private String caseNumber;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private CaseCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CaseStatus status = CaseStatus.ACTIVE;

    @Column(name = "is_historical_demo", nullable = false)
    private boolean isHistoricalDemo = false;

    @Column(name = "incident_date")
    private Instant incidentDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "caseEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EvidenceEntity> evidenceList = new ArrayList<>();

    @OneToMany(mappedBy = "caseEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimelineEventEntity> timelineEvents = new ArrayList<>();

    @OneToMany(mappedBy = "caseEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GraphNodeEntity> graphNodes = new ArrayList<>();

    @OneToMany(mappedBy = "caseEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GraphEdgeEntity> graphEdges = new ArrayList<>();

    @OneToMany(mappedBy = "caseEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnomalyEntity> anomalies = new ArrayList<>();

    @OneToOne(mappedBy = "caseEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private AiSummaryEntity aiSummary;

    public CaseEntity() {}

    public CaseEntity(String caseNumber, String title, String description, CaseCategory category, CaseStatus status, boolean isHistoricalDemo, Instant incidentDate) {
        this.caseNumber = caseNumber;
        this.title = title;
        this.description = description;
        this.category = category;
        this.status = status != null ? status : CaseStatus.ACTIVE;
        this.isHistoricalDemo = isHistoricalDemo;
        this.incidentDate = incidentDate;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public CaseCategory getCategory() { return category; }
    public void setCategory(CaseCategory category) { this.category = category; }

    public CaseStatus getStatus() { return status; }
    public void setStatus(CaseStatus status) { this.status = status; }

    public boolean isHistoricalDemo() { return isHistoricalDemo; }
    public void setHistoricalDemo(boolean historicalDemo) { isHistoricalDemo = historicalDemo; }

    public Instant getIncidentDate() { return incidentDate; }
    public void setIncidentDate(Instant incidentDate) { this.incidentDate = incidentDate; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public List<EvidenceEntity> getEvidenceList() { return evidenceList; }
    public void setEvidenceList(List<EvidenceEntity> evidenceList) { this.evidenceList = evidenceList; }

    public List<TimelineEventEntity> getTimelineEvents() { return timelineEvents; }
    public void setTimelineEvents(List<TimelineEventEntity> timelineEvents) { this.timelineEvents = timelineEvents; }

    public List<GraphNodeEntity> getGraphNodes() { return graphNodes; }
    public void setGraphNodes(List<GraphNodeEntity> graphNodes) { this.graphNodes = graphNodes; }

    public List<GraphEdgeEntity> getGraphEdges() { return graphEdges; }
    public void setGraphEdges(List<GraphEdgeEntity> graphEdges) { this.graphEdges = graphEdges; }

    public List<AnomalyEntity> getAnomalies() { return anomalies; }
    public void setAnomalies(List<AnomalyEntity> anomalies) { this.anomalies = anomalies; }

    public AiSummaryEntity getAiSummary() { return aiSummary; }
    public void setAiSummary(AiSummaryEntity aiSummary) { this.aiSummary = aiSummary; }
}
