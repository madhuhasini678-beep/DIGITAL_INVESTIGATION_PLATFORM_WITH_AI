package com.investigation.repository;

import com.investigation.entity.AnomalyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnomalyRepository extends JpaRepository<AnomalyEntity, UUID> {

    List<AnomalyEntity> findByCaseEntityIdOrderByDetectedAtDesc(UUID caseId);

    long countByCaseEntityId(UUID caseId);
}
