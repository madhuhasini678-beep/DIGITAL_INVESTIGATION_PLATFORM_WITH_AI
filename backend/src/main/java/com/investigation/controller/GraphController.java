package com.investigation.controller;

import com.investigation.dto.ApiResponse;
import com.investigation.dto.GraphPayloadResponse;
import com.investigation.service.EvidenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cases/{caseId}/graph")
@CrossOrigin(origins = "*")
public class GraphController {

    private final EvidenceService evidenceService;

    public GraphController(EvidenceService evidenceService) {
        this.evidenceService = evidenceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<GraphPayloadResponse>> getGraphPayload(@PathVariable UUID caseId) {
        GraphPayloadResponse graph = evidenceService.getGraphPayload(caseId);
        return ResponseEntity.ok(ApiResponse.ok("Relationship graph retrieved successfully", graph));
    }
}
