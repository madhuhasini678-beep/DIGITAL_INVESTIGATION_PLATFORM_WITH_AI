package com.investigation.repository;

import com.investigation.entity.GraphEdgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GraphEdgeRepository extends JpaRepository<GraphEdgeEntity, UUID> {

    List<GraphEdgeEntity> findByCaseEntityId(UUID caseId);
}
