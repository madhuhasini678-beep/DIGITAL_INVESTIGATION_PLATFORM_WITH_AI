package com.investigation.service;

import com.investigation.entity.CaseEntity;
import com.investigation.entity.GraphEdgeEntity;
import com.investigation.entity.GraphNodeEntity;
import com.investigation.entity.enums.NodeType;
import com.investigation.parser.EvidenceRow;
import com.investigation.parser.ParsedEvidenceResult;
import com.investigation.repository.GraphEdgeRepository;
import com.investigation.repository.GraphNodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class GraphExtractionService {

    private final GraphNodeRepository graphNodeRepository;
    private final GraphEdgeRepository graphEdgeRepository;

    private static final List<String[]> ENTITY_PAIRS = List.of(
            new String[]{"source", "target"},
            new String[]{"from", "to"},
            new String[]{"sender", "receiver"},
            new String[]{"buyer", "seller"},
            new String[]{"issuingbank", "receivingbank"},
            new String[]{"caller", "callee"},
            new String[]{"person", "location"},
            new String[]{"origin", "destination"}
    );

    public GraphExtractionService(GraphNodeRepository graphNodeRepository, GraphEdgeRepository graphEdgeRepository) {
        this.graphNodeRepository = graphNodeRepository;
        this.graphEdgeRepository = graphEdgeRepository;
    }

    public void extractAndPersistGraph(
            CaseEntity caseEntity,
            ParsedEvidenceResult parsedResult) {

        List<String> headers = parsedResult.getHeaders();
        String[] matchedPair = findEntityPair(headers);

        if (matchedPair == null) {
            return; // No obvious node-link entity pair in this dataset
        }

        String sourceCol = matchedPair[0];
        String targetCol = matchedPair[1];

        // Retrieve existing nodes for this case to avoid duplicates
        List<GraphNodeEntity> existingNodes = graphNodeRepository.findByCaseEntityId(caseEntity.getId());
        Map<String, GraphNodeEntity> nodeMap = new HashMap<>();
        for (GraphNodeEntity node : existingNodes) {
            nodeMap.put(node.getNodeLabel().trim().toLowerCase(), node);
        }

        List<GraphEdgeEntity> newEdges = new ArrayList<>();

        for (EvidenceRow row : parsedResult.getRows()) {
            String sourceVal = row.getValue(sourceCol);
            String targetVal = row.getValue(targetCol);

            if (sourceVal == null || sourceVal.isBlank() || targetVal == null || targetVal.isBlank()) {
                continue;
            }

            final String finalSourceVal = sourceVal.trim();
            final String finalTargetVal = targetVal.trim();

            GraphNodeEntity sourceNode = nodeMap.computeIfAbsent(finalSourceVal.toLowerCase(), k -> {
                GraphNodeEntity node = new GraphNodeEntity(caseEntity, finalSourceVal, inferNodeType(sourceCol, finalSourceVal), "{}");
                return graphNodeRepository.save(node);
            });

            GraphNodeEntity targetNode = nodeMap.computeIfAbsent(finalTargetVal.toLowerCase(), k -> {
                GraphNodeEntity node = new GraphNodeEntity(caseEntity, finalTargetVal, inferNodeType(targetCol, finalTargetVal), "{}");
                return graphNodeRepository.save(node);
            });

            String relation = inferRelation(sourceCol, targetCol);
            GraphEdgeEntity edge = new GraphEdgeEntity(caseEntity, sourceNode, targetNode, relation, 1.0, "Extracted from row " + row.getRowNumber());
            newEdges.add(edge);
        }

        if (!newEdges.isEmpty()) {
            graphEdgeRepository.saveAll(newEdges);
        }
    }

    private String[] findEntityPair(List<String> headers) {
        for (String[] pair : ENTITY_PAIRS) {
            String col1 = findMatchingHeader(headers, pair[0]);
            String col2 = findMatchingHeader(headers, pair[1]);
            if (col1 != null && col2 != null && !col1.equalsIgnoreCase(col2)) {
                return new String[]{col1, col2};
            }
        }
        return null;
    }

    private String findMatchingHeader(List<String> headers, String targetPattern) {
        for (String h : headers) {
            String clean = h.trim().toLowerCase().replaceAll("[^a-z0-9]", "");
            if (clean.equals(targetPattern) || clean.contains(targetPattern)) {
                return h;
            }
        }
        return null;
    }

    private NodeType inferNodeType(String column, String value) {
        String col = column.toLowerCase();
        if (col.contains("bank") || col.contains("acc") || col.contains("account")) return NodeType.BANK_ACCOUNT;
        if (col.contains("company") || col.contains("org") || col.contains("corp") || col.contains("firm")) return NodeType.ORGANIZATION;
        if (col.contains("loc") || col.contains("city") || col.contains("place") || col.contains("address")) return NodeType.LOCATION;
        if (col.contains("car") || col.contains("vehicle") || col.contains("van")) return NodeType.VEHICLE;
        return NodeType.PERSON;
    }

    private String inferRelation(String sourceCol, String targetCol) {
        String s = sourceCol.toLowerCase();
        if (s.contains("bank")) return "INTER_BANK_TRANSFER";
        if (s.contains("call")) return "PHONE_CALL";
        if (s.contains("buy") || s.contains("sell")) return "SECURITIES_TRANSACTION";
        return "INTERACTED_WITH";
    }
}
