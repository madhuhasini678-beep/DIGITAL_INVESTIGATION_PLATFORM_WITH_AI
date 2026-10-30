package com.investigation.service;

import com.investigation.dto.EvidencePreviewResponse;
import com.investigation.dto.EvidenceResponse;
import com.investigation.dto.GraphPayloadResponse;
import com.investigation.dto.TimelineEventResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface EvidenceService {

    EvidenceResponse uploadEvidence(UUID caseId, MultipartFile file, boolean isSimulated) throws IOException;

    List<EvidenceResponse> getEvidenceByCase(UUID caseId);

    EvidenceResponse getEvidenceById(UUID id);

    EvidencePreviewResponse getEvidencePreview(UUID id, int page, int pageSize) throws IOException;

    void deleteEvidence(UUID id);

    List<TimelineEventResponse> getTimelineEvents(UUID caseId);

    GraphPayloadResponse getGraphPayload(UUID caseId);
}
