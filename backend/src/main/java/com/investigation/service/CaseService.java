package com.investigation.service;

import com.investigation.dto.CaseCreateRequest;
import com.investigation.dto.CaseResponse;
import com.investigation.dto.CaseSummaryDto;
import com.investigation.dto.CaseUpdateRequest;
import com.investigation.entity.enums.CaseCategory;
import com.investigation.entity.enums.CaseStatus;

import java.util.List;
import java.util.UUID;

public interface CaseService {

    List<CaseResponse> getCases(CaseCategory category, CaseStatus status, String search);

    CaseResponse getCaseById(UUID id);

    CaseResponse createCase(CaseCreateRequest request);

    CaseResponse updateCase(UUID id, CaseUpdateRequest request);

    void deleteCase(UUID id);

    CaseSummaryDto getDashboardSummary();
}
