package com.investigation.dto;

import com.investigation.entity.enums.CaseCategory;
import com.investigation.entity.enums.CaseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public class CaseCreateRequest {

    @NotBlank(message = "Case title cannot be blank")
    private String title;

    private String caseNumber; // Auto-generated if omitted

    private String description;

    @NotNull(message = "Case category is required")
    private CaseCategory category;

    private CaseStatus status = CaseStatus.ACTIVE;

    private boolean isHistoricalDemo = false;

    private Instant incidentDate;

    public CaseCreateRequest() {}

    public CaseCreateRequest(String title, String caseNumber, String description, CaseCategory category, CaseStatus status, boolean isHistoricalDemo, Instant incidentDate) {
        this.title = title;
        this.caseNumber = caseNumber;
        this.description = description;
        this.category = category;
        this.status = status;
        this.isHistoricalDemo = isHistoricalDemo;
        this.incidentDate = incidentDate;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

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
}
