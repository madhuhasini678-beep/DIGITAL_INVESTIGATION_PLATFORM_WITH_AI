package com.investigation.repository;

import com.investigation.entity.GraphNodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GraphNodeRepository extends JpaRepository<GraphNodeEntity, UUID> {

    List<GraphNodeEntity> findByCaseEntityId(UUID caseId);

    long countByCaseEntityId(UUID caseId);
}
