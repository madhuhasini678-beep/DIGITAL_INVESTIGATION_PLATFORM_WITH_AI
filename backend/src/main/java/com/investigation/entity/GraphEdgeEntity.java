package com.investigation.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "graph_edges")
public class GraphEdgeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    @JsonIgnore
    private CaseEntity caseEntity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source_node_id", nullable = false)
    private GraphNodeEntity sourceNode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "target_node_id", nullable = false)
    private GraphNodeEntity targetNode;

    @Column(name = "relationship_type", nullable = false, length = 100)
    private String relationshipType;

    @Column(name = "weight", nullable = false)
    private double weight = 1.0;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public GraphEdgeEntity() {}

    public GraphEdgeEntity(CaseEntity caseEntity, GraphNodeEntity sourceNode, GraphNodeEntity targetNode, String relationshipType, double weight, String notes) {
        this.caseEntity = caseEntity;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.relationshipType = relationshipType;
        this.weight = weight;
        this.notes = notes;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public CaseEntity getCaseEntity() { return caseEntity; }
    public void setCaseEntity(CaseEntity caseEntity) { this.caseEntity = caseEntity; }

    public GraphNodeEntity getSourceNode() { return sourceNode; }
    public void setSourceNode(GraphNodeEntity sourceNode) { this.sourceNode = sourceNode; }

    public GraphNodeEntity getTargetNode() { return targetNode; }
    public void setTargetNode(GraphNodeEntity targetNode) { this.targetNode = targetNode; }

    public String getRelationshipType() { return relationshipType; }
    public void setRelationshipType(String relationshipType) { this.relationshipType = relationshipType; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
