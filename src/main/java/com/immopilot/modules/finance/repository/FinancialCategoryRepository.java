package com.immopilot.modules.finance.repository;

import com.immopilot.modules.finance.domain.FinancialCategory;
import com.immopilot.modules.finance.domain.OperationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FinancialCategoryRepository extends JpaRepository<FinancialCategory, Long> {

    @Query("SELECT c FROM FinancialCategory c WHERE c.isSystemDefault = true OR c.user.id = :userId")
    List<FinancialCategory> findAllByUserIdOrSystemDefault(@Param("userId") Long userId);

    @Query("SELECT c FROM FinancialCategory c WHERE (c.isSystemDefault = true OR c.user.id = :userId) AND c.type = :type")
    List<FinancialCategory> findAllByUserIdOrSystemDefaultAndType(@Param("userId") Long userId,
                                                                  @Param("type") OperationType type);
}
