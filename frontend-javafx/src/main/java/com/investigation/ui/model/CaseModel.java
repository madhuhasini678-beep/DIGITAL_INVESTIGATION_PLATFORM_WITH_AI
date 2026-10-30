package com.investigation.ui.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseModel {

    private String id;
    private String caseNumber;
    private String title;
    private String description;
    private String category;
    private String status;
    private boolean isHistoricalDemo;
    private String incidentDate;
    private int evidenceCount;
    private int timelineEventCount;
    private int graphNodeCount;
    private int anomalyCount;
    private boolean hasAiSummary;

    public CaseModel() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isHistoricalDemo() { return isHistoricalDemo; }
    public void setHistoricalDemo(boolean historicalDemo) { isHistoricalDemo = historicalDemo; }

    public String getIncidentDate() { return incidentDate; }
    public void setIncidentDate(String incidentDate) { this.incidentDate = incidentDate; }

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
