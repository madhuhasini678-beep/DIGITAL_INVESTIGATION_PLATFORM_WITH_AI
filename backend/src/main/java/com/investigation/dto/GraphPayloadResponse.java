package com.investigation.dto;

import com.investigation.entity.GraphEdgeEntity;
import com.investigation.entity.GraphNodeEntity;
import com.investigation.entity.enums.NodeType;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GraphPayloadResponse {

    private UUID caseId;
    private List<NodeDto> nodes;
    private List<EdgeDto> edges;

    public GraphPayloadResponse() {}

    public GraphPayloadResponse(UUID caseId, List<GraphNodeEntity> nodeEntities, List<GraphEdgeEntity> edgeEntities) {
        this.caseId = caseId;
        this.nodes = nodeEntities.stream().map(NodeDto::fromEntity).collect(Collectors.toList());
        this.edges = edgeEntities.stream().map(EdgeDto::fromEntity).collect(Collectors.toList());
    }

    public UUID getCaseId() { return caseId; }
    public void setCaseId(UUID caseId) { this.caseId = caseId; }

    public List<NodeDto> getNodes() { return nodes; }
    public void setNodes(List<NodeDto> nodes) { this.nodes = nodes; }

    public List<EdgeDto> getEdges() { return edges; }
    public void setEdges(List<EdgeDto> edges) { this.edges = edges; }

    public static class NodeDto {
        private UUID id;
        private String label;
        private NodeType type;
        private String metadata;

        public static NodeDto fromEntity(GraphNodeEntity entity) {
            NodeDto dto = new NodeDto();
            dto.setId(entity.getId());
            dto.setLabel(entity.getNodeLabel());
            dto.setType(entity.getNodeType());
            dto.setMetadata(entity.getMetadata());
            return dto;
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public NodeType getType() { return type; }
        public void setType(NodeType type) { this.type = type; }

        public String getMetadata() { return metadata; }
        public void setMetadata(String metadata) { this.metadata = metadata; }
    }

    public static class EdgeDto {
        private UUID id;
        private UUID sourceNodeId;
        private UUID targetNodeId;
        private String sourceLabel;
        private String targetLabel;
        private String relationship;
        private double weight;
        private String notes;

        public static EdgeDto fromEntity(GraphEdgeEntity entity) {
            EdgeDto dto = new EdgeDto();
            dto.setId(entity.getId());
            dto.setSourceNodeId(entity.getSourceNode().getId());
            dto.setTargetNodeId(entity.getTargetNode().getId());
            dto.setSourceLabel(entity.getSourceNode().getNodeLabel());
            dto.setTargetLabel(entity.getTargetNode().getNodeLabel());
            dto.setRelationship(entity.getRelationshipType());
            dto.setWeight(entity.getWeight());
            dto.setNotes(entity.getNotes());
            return dto;
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public UUID getSourceNodeId() { return sourceNodeId; }
        public void setSourceNodeId(UUID sourceNodeId) { this.sourceNodeId = sourceNodeId; }

        public UUID getTargetNodeId() { return targetNodeId; }
        public void setTargetNodeId(UUID targetNodeId) { this.targetNodeId = targetNodeId; }

        public String getSourceLabel() { return sourceLabel; }
        public void setSourceLabel(String sourceLabel) { this.sourceLabel = sourceLabel; }

        public String getTargetLabel() { return targetLabel; }
        public void setTargetLabel(String targetLabel) { this.targetLabel = targetLabel; }

        public String getRelationship() { return relationship; }
        public void setRelationship(String relationship) { this.relationship = relationship; }

        public double getWeight() { return weight; }
        public void setWeight(double weight) { this.weight = weight; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}
