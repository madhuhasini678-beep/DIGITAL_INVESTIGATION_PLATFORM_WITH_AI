package com.investigation.repository;

import com.investigation.entity.EvidenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EvidenceRepository extends JpaRepository<EvidenceEntity, UUID> {

    List<EvidenceEntity> findByCaseEntityId(UUID caseId);

    Optional<EvidenceEntity> findBySha256Hash(String sha256Hash);

    long countByCaseEntityId(UUID caseId);
}
