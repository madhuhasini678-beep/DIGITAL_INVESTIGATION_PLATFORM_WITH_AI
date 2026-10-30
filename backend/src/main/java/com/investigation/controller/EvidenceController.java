package com.investigation.controller;

import com.investigation.dto.ApiResponse;
import com.investigation.dto.EvidencePreviewResponse;
import com.investigation.dto.EvidenceResponse;
import com.investigation.service.EvidenceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
public class EvidenceController {

    private final EvidenceService evidenceService;

    public EvidenceController(EvidenceService evidenceService) {
        this.evidenceService = evidenceService;
    }

    @PostMapping("/api/cases/{caseId}/evidence/upload")
    public ResponseEntity<ApiResponse<EvidenceResponse>> uploadEvidence(
            @PathVariable UUID caseId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isSimulated", defaultValue = "false") boolean isSimulated) throws IOException {

        EvidenceResponse response = evidenceService.uploadEvidence(caseId, file, isSimulated);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Evidence uploaded and processed successfully", response));
    }

    @GetMapping("/api/cases/{caseId}/evidence")
    public ResponseEntity<ApiResponse<List<EvidenceResponse>>> getEvidenceByCase(@PathVariable UUID caseId) {
        List<EvidenceResponse> list = evidenceService.getEvidenceByCase(caseId);
        return ResponseEntity.ok(ApiResponse.ok("Evidence list retrieved", list));
    }

    @GetMapping("/api/evidence/{id}")
    public ResponseEntity<ApiResponse<EvidenceResponse>> getEvidenceById(@PathVariable UUID id) {
        EvidenceResponse response = evidenceService.getEvidenceById(id);
        return ResponseEntity.ok(ApiResponse.ok("Evidence retrieved", response));
    }

    @GetMapping("/api/evidence/{id}/preview")
    public ResponseEntity<ApiResponse<EvidencePreviewResponse>> getEvidencePreview(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) throws IOException {

        EvidencePreviewResponse preview = evidenceService.getEvidencePreview(id, page, size);
        return ResponseEntity.ok(ApiResponse.ok("Evidence preview retrieved", preview));
    }

    @DeleteMapping("/api/evidence/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvidence(@PathVariable UUID id) {
        evidenceService.deleteEvidence(id);
        return ResponseEntity.ok(ApiResponse.ok("Evidence deleted successfully", null));
    }
}
