package com.immopilot.modules.sales.repository;

import com.immopilot.modules.sales.domain.SaleVisit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleVisitRepository extends JpaRepository<SaleVisit, Long> {
    List<SaleVisit> findByProcessIdOrderByVisitDateDesc(Long processId);

    boolean existsByProspectId(Long prospectId);
}
