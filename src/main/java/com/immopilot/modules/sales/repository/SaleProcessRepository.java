package com.immopilot.modules.sales.repository;

import com.immopilot.modules.sales.domain.SaleProcess;
import com.immopilot.modules.sales.domain.SaleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SaleProcessRepository extends JpaRepository<SaleProcess, Long> {

    List<SaleProcess> findByPropertyId(Long propertyId);

    Optional<SaleProcess> findByPropertyIdAndUnitId(Long propertyId, Long unitId);

    // Check for active processes for a specific unit (or global if unitId is null)
    @Query("SELECT COUNT(s) > 0 FROM SaleProcess s WHERE s.propertyId = :propertyId AND (s.unitId = :unitId OR (:unitId IS NULL AND s.unitId IS NULL)) AND s.status NOT IN :excludeStatuses")
    boolean existsActiveProcess(@Param("propertyId") Long propertyId, @Param("unitId") Long unitId,
                                @Param("excludeStatuses") List<SaleStatus> excludeStatuses);

    // Check for ANY active process on the property (used to block global sale if
    // unit sale exists, or vice versa)
    @Query("SELECT COUNT(s) > 0 FROM SaleProcess s WHERE s.propertyId = :propertyId AND s.status NOT IN :excludeStatuses")
    boolean existsAnyActiveProcessOnProperty(@Param("propertyId") Long propertyId,
                                             @Param("excludeStatuses") List<SaleStatus> excludeStatuses);
}
