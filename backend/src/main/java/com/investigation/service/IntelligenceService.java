package com.investigation.service;

import com.investigation.dto.AiSummaryResponse;
import com.investigation.dto.AnomalyResponse;

import java.util.List;
import java.util.UUID;

public interface IntelligenceService {

    List<AnomalyResponse> detectAndSaveAnomalies(UUID caseId);

    List<AnomalyResponse> getAnomalies(UUID caseId);

    AiSummaryResponse generateAndSaveSummary(UUID caseId);

    AiSummaryResponse getSummary(UUID caseId);
}
