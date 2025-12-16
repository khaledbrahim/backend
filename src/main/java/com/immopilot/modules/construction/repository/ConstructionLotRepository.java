package com.immopilot.modules.construction.repository;

import com.immopilot.modules.construction.domain.ConstructionLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConstructionLotRepository extends JpaRepository<ConstructionLot, Long> {
    List<ConstructionLot> findByProjectId(Long projectId);
}
