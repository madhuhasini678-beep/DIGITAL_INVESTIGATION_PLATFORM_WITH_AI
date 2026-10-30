package com.investigation.dto;

import com.investigation.entity.AnomalyEntity;
import com.investigation.entity.enums.AnomalySeverity;

import java.time.Instant;
import java.util.UUID;

public class AnomalyResponse {

    private UUID id;
    private UUID caseId;
    private String anomalyType;
    private String title;
    private String description;
    private AnomalySeverity severity;
    private double confidenceScore;
    private Instant detectedAt;

    public AnomalyResponse() {}

    public static AnomalyResponse fromEntity(AnomalyEntity entity) {
        AnomalyResponse resp = new AnomalyResponse();
        resp.setId(entity.getId());
        resp.setCaseId(entity.getCaseEntity().getId());
        resp.setAnomalyType(entity.getAnomalyType());
        resp.setTitle(entity.getTitle());
        resp.setDescription(entity.getDescription());
        resp.setSeverity(entity.getSeverity());
        resp.setConfidenceScore(entity.getConfidenceScore());
        resp.setDetectedAt(entity.getDetectedAt());
        return resp;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCaseId() { return caseId; }
    public void setCaseId(UUID caseId) { this.caseId = caseId; }

    public String getAnomalyType() { return anomalyType; }
    public void setAnomalyType(String anomalyType) { this.anomalyType = anomalyType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public AnomalySeverity getSeverity() { return severity; }
    public void setSeverity(AnomalySeverity severity) { this.severity = severity; }

    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }

    public Instant getDetectedAt() { return detectedAt; }
    public void setDetectedAt(Instant detectedAt) { this.detectedAt = detectedAt; }
}
