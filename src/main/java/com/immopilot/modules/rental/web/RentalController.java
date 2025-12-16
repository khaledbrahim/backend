package com.immopilot.modules.rental.web;

import com.immopilot.modules.rental.domain.LeaseStatus;
import com.immopilot.modules.rental.service.RentalService;
import com.immopilot.modules.rental.service.dto.LeaseDTO;
import com.immopilot.modules.rental.service.dto.PropertyRentalStats;
import com.immopilot.modules.rental.service.dto.RentPaymentDTO;
import com.immopilot.modules.rental.service.dto.TenantDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rental")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    // --- Tenants ---

    @GetMapping("/tenants")
    public List<TenantDTO> getTenants(@RequestParam Long propertyId) {
        return rentalService.getTenantsByProperty(propertyId);
    }

    @PostMapping("/tenants")
    public ResponseEntity<TenantDTO> createTenant(@RequestParam Long propertyId, @RequestBody TenantDTO dto) {
        return ResponseEntity.ok(rentalService.createTenant(propertyId, dto));
    }

    // --- Leases ---

    @GetMapping("/leases")
    public List<LeaseDTO> getLeases(@RequestParam Long propertyId) {
        return rentalService.getLeasesByProperty(propertyId);
    }

    @PostMapping("/leases")
    public ResponseEntity<LeaseDTO> createLease(@RequestParam Long propertyId, @RequestBody LeaseDTO dto) {
        return ResponseEntity.ok(rentalService.createLease(propertyId, dto));
    }

    @PatchMapping("/leases/{id}/status")
    public ResponseEntity<LeaseDTO> updateLeaseStatus(@PathVariable Long id, @RequestParam LeaseStatus status) {
        return ResponseEntity.ok(rentalService.updateLeaseStatus(id, status));
    }

    @PostMapping("/leases/{id}/terminate")
    public ResponseEntity<LeaseDTO> terminateLease(@PathVariable Long id,
                                                   @RequestParam(required = false) java.time.LocalDate closureDate) {
        return ResponseEntity.ok(rentalService.terminateLease(id, closureDate));
    }

    // --- Payments ---

    @GetMapping("/payments")
    public List<RentPaymentDTO> getPayments(@RequestParam Long leaseId) {
        return rentalService.getPaymentsByLease(leaseId);
    }

    @PostMapping("/payments")
    public ResponseEntity<RentPaymentDTO> addPayment(@RequestParam Long leaseId, @RequestBody RentPaymentDTO dto) {
        return ResponseEntity.ok(rentalService.addPayment(leaseId, dto));
    }

    @GetMapping("/payments/{id}/receipt")
    public ResponseEntity<byte[]> getReceipt(@PathVariable Long id) {
        byte[] pdf = rentalService.generateReceipt(id);
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=\"quittance_" + id + ".pdf\"")
                .body(pdf);
    }

    @GetMapping("/stats")
    public ResponseEntity<PropertyRentalStats> getStats(@RequestParam Long propertyId) {
        return ResponseEntity.ok(rentalService.getPropertyRentalStats(propertyId));
    }
}
