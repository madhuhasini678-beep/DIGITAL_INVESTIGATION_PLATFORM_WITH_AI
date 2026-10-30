package com.investigation.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_summaries")
public class AiSummaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false, unique = true)
    @JsonIgnore
    private CaseEntity caseEntity;

    @Column(name = "factual_overview", nullable = false, columnDefinition = "TEXT")
    private String factualOverview;

    @Column(name = "key_findings", nullable = false, columnDefinition = "TEXT")
    private String keyFindings;

    @Column(name = "discrepancies_identified", columnDefinition = "TEXT")
    private String discrepanciesIdentified;

    @Column(name = "confidence_notes", columnDefinition = "TEXT")
    private String confidenceNotes;

    @Column(name = "generated_at", nullable = false, updatable = false)
    private Instant generatedAt = Instant.now();

    public AiSummaryEntity() {}

    public AiSummaryEntity(CaseEntity caseEntity, String factualOverview, String keyFindings, String discrepanciesIdentified, String confidenceNotes) {
        this.caseEntity = caseEntity;
        this.factualOverview = factualOverview;
        this.keyFindings = keyFindings;
        this.discrepanciesIdentified = discrepanciesIdentified;
        this.confidenceNotes = confidenceNotes;
        this.generatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public CaseEntity getCaseEntity() { return caseEntity; }
    public void setCaseEntity(CaseEntity caseEntity) { this.caseEntity = caseEntity; }

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
