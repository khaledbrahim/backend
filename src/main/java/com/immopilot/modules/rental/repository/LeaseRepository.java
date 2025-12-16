package com.immopilot.modules.rental.repository;

import com.immopilot.modules.rental.domain.Lease;
import com.immopilot.modules.rental.domain.LeaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaseRepository extends JpaRepository<Lease, Long> {
    List<Lease> findByPropertyId(Long propertyId);

    List<Lease> findByPropertyIdAndStatus(Long propertyId, LeaseStatus status);
}
