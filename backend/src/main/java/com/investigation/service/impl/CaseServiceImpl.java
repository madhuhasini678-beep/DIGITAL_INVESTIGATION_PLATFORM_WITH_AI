package com.investigation.service.impl;

import com.investigation.dto.CaseCreateRequest;
import com.investigation.dto.CaseResponse;
import com.investigation.dto.CaseSummaryDto;
import com.investigation.dto.CaseUpdateRequest;
import com.investigation.entity.CaseEntity;
import com.investigation.entity.enums.CaseCategory;
import com.investigation.entity.enums.CaseStatus;
import com.investigation.repository.AnomalyRepository;
import com.investigation.repository.CaseRepository;
import com.investigation.repository.EvidenceRepository;
import com.investigation.repository.TimelineEventRepository;
import com.investigation.service.CaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CaseServiceImpl implements CaseService {

    private final CaseRepository caseRepository;
    private final EvidenceRepository evidenceRepository;
    private final AnomalyRepository anomalyRepository;
    private final TimelineEventRepository timelineEventRepository;

    public CaseServiceImpl(
            CaseRepository caseRepository,
            EvidenceRepository evidenceRepository,
            AnomalyRepository anomalyRepository,
            TimelineEventRepository timelineEventRepository) {
        this.caseRepository = caseRepository;
        this.evidenceRepository = evidenceRepository;
        this.anomalyRepository = anomalyRepository;
        this.timelineEventRepository = timelineEventRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CaseResponse> getCases(CaseCategory category, CaseStatus status, String search) {
        List<CaseEntity> entities = caseRepository.searchCases(category, status, search);
        return entities.stream().map(CaseResponse::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CaseResponse getCaseById(UUID id) {
        CaseEntity entity = caseRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Case not found with ID: " + id));
        return CaseResponse.fromEntity(entity);
    }

    @Override
    public CaseResponse createCase(CaseCreateRequest request) {
        String caseNumber = request.getCaseNumber();
        if (caseNumber == null || caseNumber.trim().isEmpty()) {
            caseNumber = "CASE-" + Year.now().getValue() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }

        CaseEntity entity = new CaseEntity(
                caseNumber,
                request.getTitle(),
                request.getDescription(),
                request.getCategory(),
                request.getStatus(),
                request.isHistoricalDemo(),
                request.getIncidentDate() != null ? request.getIncidentDate() : Instant.now()
        );

        CaseEntity saved = caseRepository.save(entity);
        return CaseResponse.fromEntity(saved);
    }

    @Override
    public CaseResponse updateCase(UUID id, CaseUpdateRequest request) {
        CaseEntity entity = caseRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Case not found with ID: " + id));

        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            entity.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            entity.setCategory(request.getCategory());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        if (request.getIncidentDate() != null) {
            entity.setIncidentDate(request.getIncidentDate());
        }

        CaseEntity updated = caseRepository.save(entity);
        return CaseResponse.fromEntity(updated);
    }

    @Override
    public void deleteCase(UUID id) {
        if (!caseRepository.existsById(id)) {
            throw new NoSuchElementException("Case not found with ID: " + id);
        }
        caseRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public CaseSummaryDto getDashboardSummary() {
        long totalCases = caseRepository.count();
        long activeCases = caseRepository.countByStatus(CaseStatus.ACTIVE);
        long totalEvidence = evidenceRepository.count();
        long totalAnomalies = anomalyRepository.count();
        long totalEvents = timelineEventRepository.count();

        return new CaseSummaryDto(totalCases, activeCases, totalEvidence, totalAnomalies, totalEvents);
    }
}
