package com.investigation.controller;

import com.investigation.dto.ApiResponse;
import com.investigation.dto.TimelineEventResponse;
import com.investigation.service.EvidenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cases/{caseId}/timeline")
@CrossOrigin(origins = "*")
public class TimelineController {

    private final EvidenceService evidenceService;

    public TimelineController(EvidenceService evidenceService) {
        this.evidenceService = evidenceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TimelineEventResponse>>> getTimelineEvents(@PathVariable UUID caseId) {
        List<TimelineEventResponse> events = evidenceService.getTimelineEvents(caseId);
        return ResponseEntity.ok(ApiResponse.ok("Timeline events retrieved successfully", events));
    }
}
