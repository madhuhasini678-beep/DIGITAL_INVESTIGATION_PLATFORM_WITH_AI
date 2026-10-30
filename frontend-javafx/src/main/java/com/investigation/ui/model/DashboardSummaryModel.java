package com.investigation.ui.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DashboardSummaryModel {

    private long totalCases;
    private long activeCases;
    private long totalEvidenceFiles;
    private long totalAnomaliesDetected;
    private long totalTimelineEvents;

    public DashboardSummaryModel() {}

    public long getTotalCases() { return totalCases; }
    public void setTotalCases(long totalCases) { this.totalCases = totalCases; }

    public long getActiveCases() { return activeCases; }
    public void setActiveCases(long activeCases) { this.activeCases = activeCases; }

    public long getTotalEvidenceFiles() { return totalEvidenceFiles; }
    public void setTotalEvidenceFiles(long totalEvidenceFiles) { this.totalEvidenceFiles = totalEvidenceFiles; }

    public long getTotalAnomaliesDetected() { return totalAnomaliesDetected; }
    public void setTotalAnomaliesDetected(long totalAnomaliesDetected) { this.totalAnomaliesDetected = totalAnomaliesDetected; }

    public long getTotalTimelineEvents() { return totalTimelineEvents; }
    public void setTotalTimelineEvents(long totalTimelineEvents) { this.totalTimelineEvents = totalTimelineEvents; }
}
