package com.immopilot.modules.sales.repository;

import com.immopilot.modules.sales.domain.SaleOffer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleOfferRepository extends JpaRepository<SaleOffer, Long> {
    List<SaleOffer> findByProcessIdOrderByOfferDateDesc(Long processId);
}
