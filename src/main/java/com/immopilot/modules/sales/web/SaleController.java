package com.immopilot.modules.sales.web;

import com.immopilot.modules.sales.domain.OfferStatus;
import com.immopilot.modules.sales.domain.SaleOffer;
import com.immopilot.modules.sales.domain.SaleProcess;
import com.immopilot.modules.sales.domain.SaleProspect;
import com.immopilot.modules.sales.domain.SaleStatus;
import com.immopilot.modules.sales.domain.SaleVisit;
import com.immopilot.modules.sales.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    // --- PROCESSES ---

    @PostMapping
    public ResponseEntity<SaleProcess> createProcess(@RequestBody SaleProcess process) {
        return ResponseEntity.ok(saleService.createProcess(process));
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<SaleProcess>> getProcessesByProperty(@PathVariable Long propertyId) {
        return ResponseEntity.ok(saleService.getProcessesByProperty(propertyId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleProcess> getProcess(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.getProcess(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<SaleProcess> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> statusMap) {
        SaleStatus status = SaleStatus.valueOf(statusMap.get("status"));
        return ResponseEntity.ok(saleService.updateStatus(id, status));
    }

    @PutMapping("/{id}/abandon")
    public ResponseEntity<SaleProcess> abandonProcess(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        return ResponseEntity.ok(saleService.abandonProcess(id, reason));
    }

    @PostMapping("/{id}/calculate")
    public ResponseEntity<SaleProcess> calculateIndicators(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.calculateIndicators(id));
    }

    // --- PROSPECTS ---

    @PostMapping("/prospects")
    public ResponseEntity<SaleProspect> addProspect(@RequestBody SaleProspect prospect) {
        return ResponseEntity.ok(saleService.addProspect(prospect));
    }

    @GetMapping("/{id}/prospects")
    public ResponseEntity<List<SaleProspect>> getProspects(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.getProspects(id));
    }

    // --- VISITS ---

    @PostMapping("/visits")
    public ResponseEntity<SaleVisit> addVisit(@RequestBody SaleVisit visit) {
        return ResponseEntity.ok(saleService.addVisit(visit));
    }

    @GetMapping("/{id}/visits")
    public ResponseEntity<List<SaleVisit>> getVisits(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.getVisits(id));
    }

    // --- OFFERS ---

    @PostMapping("/offers")
    public ResponseEntity<SaleOffer> addOffer(@RequestBody SaleOffer offer) {
        return ResponseEntity.ok(saleService.addOffer(offer));
    }

    @GetMapping("/{id}/offers")
    public ResponseEntity<List<SaleOffer>> getOffers(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.getOffers(id));
    }

    @PutMapping("/offers/{offerId}/status")
    public ResponseEntity<SaleOffer> updateOfferStatus(@PathVariable Long offerId,
                                                       @RequestBody Map<String, String> statusMap) {
        OfferStatus status = OfferStatus.valueOf(statusMap.get("status"));
        return ResponseEntity.ok(saleService.updateOfferStatus(offerId, status));
    }

    @GetMapping("/{id}/recommendation")
    public ResponseEntity<com.immopilot.modules.sales.service.dto.SaleRecommendationDTO> getRecommendation(
            @PathVariable Long id) {
        return ResponseEntity.ok(saleService.getRecommendation(id));
    }
}
