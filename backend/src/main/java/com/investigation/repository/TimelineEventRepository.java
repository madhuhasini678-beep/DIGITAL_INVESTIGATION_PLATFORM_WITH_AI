package com.investigation.repository;

import com.investigation.entity.TimelineEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TimelineEventRepository extends JpaRepository<TimelineEventEntity, UUID> {

    List<TimelineEventEntity> findByCaseEntityIdOrderByEventTimestampAsc(UUID caseId);

    List<TimelineEventEntity> findByEvidenceEntityId(UUID evidenceId);

    long countByCaseEntityId(UUID caseId);
}
