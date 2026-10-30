package com.investigation.controller;

import com.investigation.dto.AiSummaryResponse;
import com.investigation.dto.AnomalyResponse;
import com.investigation.dto.ApiResponse;
import com.investigation.service.IntelligenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
public class IntelligenceController {

    private final IntelligenceService intelligenceService;

    public IntelligenceController(IntelligenceService intelligenceService) {
        this.intelligenceService = intelligenceService;
    }

    @GetMapping("/api/cases/{caseId}/anomalies")
    public ResponseEntity<ApiResponse<List<AnomalyResponse>>> getAnomalies(@PathVariable UUID caseId) {
        List<AnomalyResponse> list = intelligenceService.getAnomalies(caseId);
        return ResponseEntity.ok(ApiResponse.ok("Anomalies retrieved successfully", list));
    }

    @PostMapping("/api/cases/{caseId}/anomalies/detect")
    public ResponseEntity<ApiResponse<List<AnomalyResponse>>> detectAnomalies(@PathVariable UUID caseId) {
        List<AnomalyResponse> list = intelligenceService.detectAndSaveAnomalies(caseId);
        return ResponseEntity.ok(ApiResponse.ok("Anomaly detection analysis complete", list));
    }

    @GetMapping("/api/cases/{caseId}/summary")
    public ResponseEntity<ApiResponse<AiSummaryResponse>> getSummary(@PathVariable UUID caseId) {
        AiSummaryResponse summary = intelligenceService.getSummary(caseId);
        return ResponseEntity.ok(ApiResponse.ok("AI summary retrieved successfully", summary));
    }

    @PostMapping("/api/cases/{caseId}/summary/generate")
    public ResponseEntity<ApiResponse<AiSummaryResponse>> generateSummary(@PathVariable UUID caseId) {
        AiSummaryResponse summary = intelligenceService.generateAndSaveSummary(caseId);
        return ResponseEntity.ok(ApiResponse.ok("AI investigation summary generated successfully", summary));
    }
}
