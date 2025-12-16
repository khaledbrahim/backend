package com.immopilot.modules.sales.repository;

import com.immopilot.modules.sales.domain.SaleProspect;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleProspectRepository extends JpaRepository<SaleProspect, Long> {
    List<SaleProspect> findByProcessId(Long processId);
}
