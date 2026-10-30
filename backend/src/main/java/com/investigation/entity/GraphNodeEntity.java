package com.investigation.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.investigation.entity.enums.NodeType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "graph_nodes")
public class GraphNodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    @JsonIgnore
    private CaseEntity caseEntity;

    @Column(name = "node_label", nullable = false, length = 255)
    private String nodeLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "node_type", nullable = false, length = 50)
    private NodeType nodeType;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON representation of extra node attributes

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public GraphNodeEntity() {}

    public GraphNodeEntity(CaseEntity caseEntity, String nodeLabel, NodeType nodeType, String metadata) {
        this.caseEntity = caseEntity;
        this.nodeLabel = nodeLabel;
        this.nodeType = nodeType;
        this.metadata = metadata;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public CaseEntity getCaseEntity() { return caseEntity; }
    public void setCaseEntity(CaseEntity caseEntity) { this.caseEntity = caseEntity; }

    public String getNodeLabel() { return nodeLabel; }
    public void setNodeLabel(String nodeLabel) { this.nodeLabel = nodeLabel; }

    public NodeType getNodeType() { return nodeType; }
    public void setNodeType(NodeType nodeType) { this.nodeType = nodeType; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
