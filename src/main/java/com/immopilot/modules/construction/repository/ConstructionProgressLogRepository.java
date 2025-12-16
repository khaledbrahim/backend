package com.immopilot.modules.construction.repository;

import com.immopilot.modules.construction.domain.ConstructionProgressLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConstructionProgressLogRepository extends JpaRepository<ConstructionProgressLog, Long> {
    List<ConstructionProgressLog> findByLotIdOrderByLogDateDesc(Long lotId);
}
