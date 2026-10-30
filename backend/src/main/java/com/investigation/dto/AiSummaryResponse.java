package com.investigation.dto;

import com.investigation.entity.AiSummaryEntity;
import java.time.Instant;
import java.util.UUID;

public class AiSummaryResponse {

    private UUID id;
    private UUID caseId;
    private String factualOverview;
    private String keyFindings;
    private String discrepanciesIdentified;
    private String confidenceNotes;
    private Instant generatedAt;

    public AiSummaryResponse() {}

    public static AiSummaryResponse fromEntity(AiSummaryEntity entity) {
        AiSummaryResponse resp = new AiSummaryResponse();
        resp.setId(entity.getId());
        resp.setCaseId(entity.getCaseEntity().getId());
        resp.setFactualOverview(entity.getFactualOverview());
        resp.setKeyFindings(entity.getKeyFindings());
        resp.setDiscrepanciesIdentified(entity.getDiscrepanciesIdentified());
        resp.setConfidenceNotes(entity.getConfidenceNotes());
        resp.setGeneratedAt(entity.getGeneratedAt());
        return resp;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCaseId() { return caseId; }
    public void setCaseId(UUID caseId) { this.caseId = caseId; }

    public String getFactualOverview() { return factualOverview; }
    public void setFactualOverview(String factualOverview) { this.factualOverview = factualOverview; }

    public String getKeyFindings() { return keyFindings; }
    public void setKeyFindings(String keyFindings) { this.keyFindings = keyFindings; }

    public String getDiscrepanciesIdentified() { return discrepanciesIdentified; }
    public void setDiscrepanciesIdentified(String discrepanciesIdentified) { this.discrepanciesIdentified = discrepanciesIdentified; }

    public String getConfidenceNotes() { return confidenceNotes; }
    public void setConfidenceNotes(String confidenceNotes) { this.confidenceNotes = confidenceNotes; }

    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }
}
