package com.immopilot.modules.finance.web;

import com.immopilot.modules.finance.domain.OperationType;
import com.immopilot.modules.finance.service.FinanceAlertService;
import com.immopilot.modules.finance.service.FinanceAnalyticsService;
import com.immopilot.modules.finance.service.FinanceService;
import com.immopilot.modules.finance.service.dto.CashflowStats;
import com.immopilot.modules.finance.service.dto.FinanceAnalyticsDTO;
import com.immopilot.modules.finance.service.dto.FinancialAlertDTO;
import com.immopilot.modules.finance.service.dto.FinancialCategoryDTO;
import com.immopilot.modules.finance.service.dto.OperationRequest;
import com.immopilot.modules.finance.service.dto.OperationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;
    private final FinanceAlertService alertService;
    private final FinanceAnalyticsService analyticsService;

    @PostMapping(value = "/properties/{propertyId}/operations", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<OperationResponse> createOperation(
            @PathVariable Long propertyId,
            @RequestPart("data") OperationRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        // Ensure request propertyId matches path
        request.setPropertyId(propertyId);
        return ResponseEntity.ok(financeService.createOperation(request, file));
    }

    @GetMapping("/properties/{propertyId}/operations")
    public ResponseEntity<List<OperationResponse>> getOperations(
            @PathVariable Long propertyId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) OperationType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(financeService.getOperations(propertyId, categoryId, type, startDate, endDate));
    }

    @GetMapping("/operations")
    public ResponseEntity<List<OperationResponse>> getGlobalOperations(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) OperationType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(financeService.getOperations(null, categoryId, type, startDate, endDate));
    }

    @GetMapping("/properties/{propertyId}/stats")
    public ResponseEntity<CashflowStats> getStats(
            @PathVariable Long propertyId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(financeService.getStats(propertyId, year, month));
    }

    @GetMapping("/stats")
    public ResponseEntity<CashflowStats> getGlobalStats(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(financeService.getStats(null, year, month));
    }

    @PutMapping("/operations/{id}")
    public ResponseEntity<OperationResponse> updateOperation(
            @PathVariable Long id,
            @RequestBody OperationRequest request) {
        return ResponseEntity.ok(financeService.updateOperation(id, request));
    }

    @DeleteMapping("/operations/{id}")
    public ResponseEntity<Void> deleteOperation(@PathVariable Long id) {
        financeService.deleteOperation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories")
    public ResponseEntity<List<FinancialCategoryDTO>> getCategories() {
        return ResponseEntity.ok(financeService.getCategories());
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<FinancialAlertDTO>> getAlerts() {
        return ResponseEntity.ok(alertService.getAlerts());
    }

    @GetMapping("/properties/{propertyId}/analytics")
    public ResponseEntity<FinanceAnalyticsDTO> getAnalytics(@PathVariable Long propertyId) {
        return ResponseEntity.ok(analyticsService.getAnalytics(propertyId));
    }
}
