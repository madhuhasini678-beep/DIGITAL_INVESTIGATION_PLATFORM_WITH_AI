package com.investigation.dto;

import com.investigation.entity.CaseEntity;
import com.investigation.entity.enums.CaseCategory;
import com.investigation.entity.enums.CaseStatus;
import java.time.Instant;
import java.util.UUID;

public class CaseResponse {

    private UUID id;
    private String caseNumber;
    private String title;
    private String description;
    private CaseCategory category;
    private CaseStatus status;
    private boolean isHistoricalDemo;
    private Instant incidentDate;
    private Instant createdAt;
    private Instant updatedAt;
    private int evidenceCount;
    private int timelineEventCount;
    private int graphNodeCount;
    private int anomalyCount;
    private boolean hasAiSummary;

    public CaseResponse() {}

    public static CaseResponse fromEntity(CaseEntity entity) {
        CaseResponse res = new CaseResponse();
        res.setId(entity.getId());
        res.setCaseNumber(entity.getCaseNumber());
        res.setTitle(entity.getTitle());
        res.setDescription(entity.getDescription());
        res.setCategory(entity.getCategory());
        res.setStatus(entity.getStatus());
        res.setHistoricalDemo(entity.isHistoricalDemo());
        res.setIncidentDate(entity.getIncidentDate());
        res.setCreatedAt(entity.getCreatedAt());
        res.setUpdatedAt(entity.getUpdatedAt());
        res.setEvidenceCount(entity.getEvidenceList() != null ? entity.getEvidenceList().size() : 0);
        res.setTimelineEventCount(entity.getTimelineEvents() != null ? entity.getTimelineEvents().size() : 0);
        res.setGraphNodeCount(entity.getGraphNodes() != null ? entity.getGraphNodes().size() : 0);
        res.setAnomalyCount(entity.getAnomalies() != null ? entity.getAnomalies().size() : 0);
        res.setHasAiSummary(entity.getAiSummary() != null);
        return res;
    }

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

    public int getEvidenceCount() { return evidenceCount; }
    public void setEvidenceCount(int evidenceCount) { this.evidenceCount = evidenceCount; }

    public int getTimelineEventCount() { return timelineEventCount; }
    public void setTimelineEventCount(int timelineEventCount) { this.timelineEventCount = timelineEventCount; }

    public int getGraphNodeCount() { return graphNodeCount; }
    public void setGraphNodeCount(int graphNodeCount) { this.graphNodeCount = graphNodeCount; }

    public int getAnomalyCount() { return anomalyCount; }
    public void setAnomalyCount(int anomalyCount) { this.anomalyCount = anomalyCount; }

    public boolean isHasAiSummary() { return hasAiSummary; }
    public void setHasAiSummary(boolean hasAiSummary) { this.hasAiSummary = hasAiSummary; }
}
