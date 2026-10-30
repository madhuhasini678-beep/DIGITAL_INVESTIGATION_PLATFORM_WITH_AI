package com.investigation.dto;

import com.investigation.entity.enums.CaseCategory;
import com.investigation.entity.enums.CaseStatus;
import java.time.Instant;

public class CaseUpdateRequest {

    private String title;
    private String description;
    private CaseCategory category;
    private CaseStatus status;
    private Instant incidentDate;

    public CaseUpdateRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public CaseCategory getCategory() { return category; }
    public void setCategory(CaseCategory category) { this.category = category; }

    public CaseStatus getStatus() { return status; }
    public void setStatus(CaseStatus status) { this.status = status; }

    public Instant getIncidentDate() { return incidentDate; }
    public void setIncidentDate(Instant incidentDate) { this.incidentDate = incidentDate; }
}
