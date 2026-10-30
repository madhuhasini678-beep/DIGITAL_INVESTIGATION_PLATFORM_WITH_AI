package com.investigation.repository;

import com.investigation.entity.CaseEntity;
import com.investigation.entity.enums.CaseCategory;
import com.investigation.entity.enums.CaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CaseRepository extends JpaRepository<CaseEntity, UUID> {

    Optional<CaseEntity> findByCaseNumber(String caseNumber);

    List<CaseEntity> findByCategory(CaseCategory category);

    List<CaseEntity> findByStatus(CaseStatus status);

    long countByStatus(CaseStatus status);

    @Query("SELECT c FROM CaseEntity c WHERE " +
           "(:category IS NULL OR c.category = :category) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:search IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.caseNumber) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY c.createdAt DESC")
    List<CaseEntity> searchCases(
            @Param("category") CaseCategory category,
            @Param("status") CaseStatus status,
            @Param("search") String search
    );
}
