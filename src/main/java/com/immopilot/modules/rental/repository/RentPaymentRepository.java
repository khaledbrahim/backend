package com.immopilot.modules.rental.repository;

import com.immopilot.modules.rental.domain.RentPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RentPaymentRepository extends JpaRepository<RentPayment, Long> {
    List<RentPayment> findByLeaseId(Long leaseId);

    List<RentPayment> findByLeaseIdOrderByPaymentDateDesc(Long leaseId);
}
