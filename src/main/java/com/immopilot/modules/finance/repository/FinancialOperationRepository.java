package com.immopilot.modules.finance.repository;

import com.immopilot.modules.finance.domain.FinancialOperation;
import com.immopilot.modules.finance.domain.OperationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FinancialOperationRepository extends JpaRepository<FinancialOperation, Long> {

    List<FinancialOperation> findByPropertyId(Long propertyId);

    long countByPropertyId(Long propertyId);

    List<FinancialOperation> findByPropertyIdAndOperationDateBetween(Long propertyId, LocalDate startDate,
                                                                     LocalDate endDate);

    // Check subscription limit - count operations for a user across all properties
    @Query("SELECT count(fo) FROM FinancialOperation fo WHERE fo.property.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT fo FROM FinancialOperation fo WHERE fo.property.user.id = :userId")
    List<FinancialOperation> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT o FROM FinancialOperation o WHERE o.property.id = :propertyId AND (:categoryId IS NULL OR o.category.id = :categoryId) AND (:type IS NULL OR o.operationType = :type) AND (cast(:startDate as date) IS NULL OR o.operationDate >= :startDate) AND (cast(:endDate as date) IS NULL OR o.operationDate <= :endDate)")
    List<FinancialOperation> findWithFilters(Long propertyId, Long categoryId, OperationType type,
                                             LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(fo.amount) FROM FinancialOperation fo WHERE fo.sourceReference = :ref AND fo.operationType = 'EXPENSE'")
    java.math.BigDecimal sumAmountBySourceReference(@Param("ref") String ref);
}
