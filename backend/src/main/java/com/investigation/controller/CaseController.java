package com.investigation.controller;

import com.investigation.dto.ApiResponse;
import com.investigation.dto.CaseCreateRequest;
import com.investigation.dto.CaseResponse;
import com.investigation.dto.CaseSummaryDto;
import com.investigation.dto.CaseUpdateRequest;
import com.investigation.entity.enums.CaseCategory;
import com.investigation.entity.enums.CaseStatus;
import com.investigation.service.CaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cases")
@CrossOrigin(origins = "*") // Allows local desktop client cross-origin access
public class CaseController {

    private final CaseService caseService;

    public CaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CaseResponse>>> getCases(
            @RequestParam(required = false) CaseCategory category,
            @RequestParam(required = false) CaseStatus status,
            @RequestParam(required = false) String search) {
        List<CaseResponse> cases = caseService.getCases(category, status, search);
        return ResponseEntity.ok(ApiResponse.ok("Cases retrieved successfully", cases));
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<ApiResponse<CaseSummaryDto>> getDashboardSummary() {
        CaseSummaryDto summary = caseService.getDashboardSummary();
        return ResponseEntity.ok(ApiResponse.ok("Dashboard summary retrieved successfully", summary));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CaseResponse>> getCaseById(@PathVariable UUID id) {
        CaseResponse response = caseService.getCaseById(id);
        return ResponseEntity.ok(ApiResponse.ok("Case retrieved successfully", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CaseResponse>> createCase(@Valid @RequestBody CaseCreateRequest request) {
        CaseResponse created = caseService.createCase(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Case created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CaseResponse>> updateCase(
            @PathVariable UUID id,
            @RequestBody CaseUpdateRequest request) {
        CaseResponse updated = caseService.updateCase(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Case updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCase(@PathVariable UUID id) {
        caseService.deleteCase(id);
        return ResponseEntity.ok(ApiResponse.ok("Case deleted successfully", null));
    }
}
