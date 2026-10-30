package com.investigation.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.investigation.entity.enums.AnomalySeverity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "anomaly_records")
public class AnomalyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    @JsonIgnore
    private CaseEntity caseEntity;

    @Column(name = "anomaly_type", nullable = false, length = 100)
    private String anomalyType;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private AnomalySeverity severity = AnomalySeverity.MEDIUM;

    @Column(name = "confidence_score", nullable = false)
    private double confidenceScore = 0.0;

    @Column(name = "evidence_references", columnDefinition = "TEXT")
    private String evidenceReferences; // JSON array of record/evidence IDs

    @Column(name = "detected_at", nullable = false, updatable = false)
    private Instant detectedAt = Instant.now();

    public AnomalyEntity() {}

    public AnomalyEntity(CaseEntity caseEntity, String anomalyType, String title, String description, AnomalySeverity severity, double confidenceScore, String evidenceReferences) {
        this.caseEntity = caseEntity;
        this.anomalyType = anomalyType;
        this.title = title;
        this.description = description;
        this.severity = severity != null ? severity : AnomalySeverity.MEDIUM;
        this.confidenceScore = confidenceScore;
        this.evidenceReferences = evidenceReferences;
        this.detectedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public CaseEntity getCaseEntity() { return caseEntity; }
    public void setCaseEntity(CaseEntity caseEntity) { this.caseEntity = caseEntity; }

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

    public String getEvidenceReferences() { return evidenceReferences; }
    public void setEvidenceReferences(String evidenceReferences) { this.evidenceReferences = evidenceReferences; }

    public Instant getDetectedAt() { return detectedAt; }
    public void setDetectedAt(Instant detectedAt) { this.detectedAt = detectedAt; }
}
