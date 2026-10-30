package com.investigation.repository;

import com.investigation.entity.AiSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiSummaryRepository extends JpaRepository<AiSummaryEntity, UUID> {

    Optional<AiSummaryEntity> findByCaseEntityId(UUID caseId);
}
