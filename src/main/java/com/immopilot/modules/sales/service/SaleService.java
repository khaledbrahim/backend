package com.immopilot.modules.sales.service;

import com.immopilot.modules.construction.service.ConstructionService;
import com.immopilot.modules.construction.service.dto.ConstructionProjectDTO;
import com.immopilot.modules.finance.service.FinanceService;
import com.immopilot.modules.finance.service.dto.CashflowStats;
import com.immopilot.modules.properties.domain.Property;
import com.immopilot.modules.properties.repository.PropertyRepository;
import com.immopilot.modules.sales.domain.OfferStatus;
import com.immopilot.modules.sales.domain.SaleOffer;
import com.immopilot.modules.sales.domain.SaleProcess;
import com.immopilot.modules.sales.domain.SaleProspect;
import com.immopilot.modules.sales.domain.SaleStatus;
import com.immopilot.modules.sales.domain.SaleVisit;
import com.immopilot.modules.sales.repository.SaleOfferRepository;
import com.immopilot.modules.sales.repository.SaleProcessRepository;
import com.immopilot.modules.sales.repository.SaleProspectRepository;
import com.immopilot.modules.sales.repository.SaleVisitRepository;
import com.immopilot.modules.users.repository.AuditLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleService {

    private static final List<SaleStatus> INACTIVE_STATUSES = Arrays.asList(SaleStatus.ACT_SIGNED,
            SaleStatus.CANCELLED);
    private final SaleProcessRepository processRepository;
    private final SaleProspectRepository prospectRepository;
    private final SaleVisitRepository visitRepository;
    private final SaleOfferRepository offerRepository;
    private final AuditLogRepository auditLogRepository;
    private final PropertyRepository propertyRepository;
    private final ConstructionService constructionService;
    private final FinanceService financeService;

    // --- PROCESSS MANAGEMENT ---

    /**
     * Creates a new Sale Process.
     * Strict Validation:
     * 1. If selling a Unit: No active Global Sale allowed. No active sale for this
     * specific Unit allowed.
     * 2. If selling Global (Immeuble): No active sale allowed for ANY unit or
     * Global.
     */
    @Transactional
    public SaleProcess createProcess(SaleProcess process) {
        // Validation logic
        if (process.getUnitId() != null) {
            // Unit Sale
            // Check for specific unit active sale
            boolean unitActive = processRepository.existsActiveProcess(process.getPropertyId(), process.getUnitId(),
                    INACTIVE_STATUSES);
            if (unitActive) {
                throw new IllegalStateException("A sale process is already active for this unit.");
            }
            // Check for Global active sale
            boolean globalActive = processRepository.existsActiveProcess(process.getPropertyId(), null,
                    INACTIVE_STATUSES);
            if (globalActive) {
                throw new IllegalStateException(
                        "A global sale process is already active for this property. Cannot sell individual unit.");
            }
        } else {
            // Global Sale
            // Check for ANY active sale (Global or Unit)
            boolean anyActive = processRepository.existsAnyActiveProcessOnProperty(process.getPropertyId(),
                    INACTIVE_STATUSES);
            if (anyActive) {
                throw new IllegalStateException(
                        "Active sale processes exist for this property (Global or Units). Cannot start a new global sale.");
            }
        }

        // Initialize
        if (process.getStatus() == null) {
            process.setStatus(SaleStatus.DRAFT);
        }
        SaleProcess saved = processRepository.save(process);
        logAudit("CREATE_PROCESS", "Created sale process ID " + saved.getId());
        return saved;
    }

    public List<SaleProcess> getProcessesByProperty(Long propertyId) {
        return processRepository.findByPropertyId(propertyId);
    }

    public SaleProcess getProcess(Long id) {
        return processRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Process not found"));
    }

    @Transactional
    public SaleProcess updateStatus(Long processId, SaleStatus newStatus) {
        SaleProcess process = getProcess(processId);

        // Strict Rule: Cannot change status if already ACT_SIGNED (Final State)
        if (process.getStatus() == SaleStatus.ACT_SIGNED && newStatus != SaleStatus.ACT_SIGNED) {
            throw new IllegalStateException("Sale is finalized (Acte Signé). Cannot change status.");
        }

        SaleStatus oldStatus = process.getStatus();
        process.setStatus(newStatus);

        if (newStatus == SaleStatus.ACT_SIGNED) {
            process.setClosingDate(java.time.LocalDate.now());
        }

        // Auto-calculate on status change (e.g., getting closer to final numbers)
        calculateIndicators(processId);

        SaleProcess saved = processRepository.save(process);
        logAudit("UPDATE_STATUS", "Process " + processId + " status changed from " + oldStatus + " to " + newStatus);
        return saved;
    }

    @Transactional
    public SaleProcess abandonProcess(Long processId, String reason) {
        SaleProcess process = getProcess(processId);
        if (process.getStatus() == SaleStatus.ACT_SIGNED) {
            throw new IllegalStateException("Cannot abandon a finalized sale.");
        }
        process.setStatus(SaleStatus.ABANDONED);
        process.setAbandonReason(reason);
        SaleProcess saved = processRepository.save(process);
        logAudit("ABANDON_PROCESS", "Abandoned process " + processId + ". Reason: " + reason);
        return saved;
    }

    // --- PROSPECT MANAGEMENT ---

    // --- PROSPECT MANAGEMENT ---

    public SaleProspect addProspect(SaleProspect prospect) {
        // Validation: Process active
        if (prospect.getProcessId() != null) {
            SaleProcess process = getProcess(prospect.getProcessId());
            if (INACTIVE_STATUSES.contains(process.getStatus()) || process.getStatus() == SaleStatus.ABANDONED) {
                throw new IllegalStateException("Cannot add prospect. Sale process is closed or abandoned.");
            }
        }
        return prospectRepository.save(prospect);
    }

    public List<SaleProspect> getProspects(Long processId) {
        return prospectRepository.findByProcessId(processId);
    }

    // --- VISIT MANAGEMENT ---

    @Transactional
    public SaleVisit addVisit(SaleVisit visit) {
        SaleProcess process = getProcess(visit.getProcessId());

        // Strict Rule: Visits allowed ONLY if ON_MARKET or VISITS
        if (process.getStatus() != SaleStatus.ON_MARKET && process.getStatus() != SaleStatus.VISITS) {
            throw new IllegalStateException(
                    "Cannot add visit. Status must be 'On Market' or 'Visits in Progress'. Current: "
                            + process.getStatus());
        }

        SaleVisit saved = visitRepository.save(visit);

        // Auto-update: ON_MARKET -> VISITS
        if (process.getStatus() == SaleStatus.ON_MARKET) {
            process.setStatus(SaleStatus.VISITS);
            processRepository.save(process);
        }

        logAudit("ADD_VISIT", "Visit added for prospect " + visit.getProspectId());
        return saved;
    }

    public List<SaleVisit> getVisits(Long processId) {
        return visitRepository.findByProcessIdOrderByVisitDateDesc(processId);
    }

    // --- OFFER MANAGEMENT ---

    @Transactional
    public SaleOffer addOffer(SaleOffer offer) {
        SaleProcess process = getProcess(offer.getProcessId());

        // 1. Validation: Process Status (Negative list)
        // Allowed: ON_MARKET, VISITS, OFFERS, NEGOTIATION
        // Blocked: OFFER_ACCEPTED, PROMESSE_SIGNED, ACT_SIGNED, ABANDONED, CANCELLED
        if (process.getStatus() == SaleStatus.OFFER_ACCEPTED ||
                process.getStatus() == SaleStatus.PROMESSE_SIGNED ||
                process.getStatus() == SaleStatus.ACT_SIGNED ||
                process.getStatus() == SaleStatus.ABANDONED ||
                process.getStatus() == SaleStatus.CANCELLED) {
            throw new IllegalStateException("Cannot add offer. Sale process is closed or waiting for signature.");
        }

        // 2. Validation: Visit Requirement
        boolean hasVisited = visitRepository.existsByProspectId(offer.getProspectId());
        if (!hasVisited) {
            throw new IllegalStateException("Prospect must visit the property before making an offer.");
        }

        // 3. check duplicate active offer logic could be here if needed

        SaleOffer saved = offerRepository.save(offer);

        // Auto-update: ON_MARKET/VISITS -> OFFERS
        if (process.getStatus() == SaleStatus.ON_MARKET || process.getStatus() == SaleStatus.VISITS) {
            process.setStatus(SaleStatus.OFFERS);
            processRepository.save(process);
        }

        logAudit("ADD_OFFER", "New offer added for process " + process.getId() + " amount: " + offer.getOfferAmount());
        return saved;
    }

    public List<SaleOffer> getOffers(Long processId) {
        return offerRepository.findByProcessIdOrderByOfferDateDesc(processId);
    }

    @Transactional
    public SaleOffer updateOfferStatus(Long offerId, OfferStatus status) {
        SaleOffer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found"));

        SaleProcess process = getProcess(offer.getProcessId());

        // Strict: Cannot change offer status if Process is already locked/finalized
        // Only allow if we are NOT yet in a closed state, OR if we are just rejecting
        // others?
        // Actually, if we accepted one, we shouldn't be able to accept another.
        if (process.getStatus() == SaleStatus.ACT_SIGNED || process.getStatus() == SaleStatus.ABANDONED) {
            throw new IllegalStateException("Cannot change offer status. Sale is finalized or abandoned.");
        }

        // If we are accepting this offer, check if process is already in a locked state
        // (PROMESSE_SIGNED)
        if (status == OfferStatus.ACCEPTED && (process.getStatus() == SaleStatus.PROMESSE_SIGNED
                || process.getStatus() == SaleStatus.OFFER_ACCEPTED)) {
            throw new IllegalStateException("An offer is already accepted for this process.");
        }

        offer.setStatus(status);
        SaleOffer saved = offerRepository.save(offer);

        if (status == OfferStatus.ACCEPTED) {
            // Reject other pending offers? (Optional but good UX)
            // For now, we leave them pending but the process status will prevent accepting
            // them.

            // Update Process Status -> PROMESSE_SIGNED (Strong Lock)
            process.setStatus(SaleStatus.PROMESSE_SIGNED);

            // Set Net Price
            BigDecimal netPrice = offer.getOfferAmount();
            if (process.getAgencyFee() != null) {
                netPrice = netPrice.subtract(process.getAgencyFee());
            }
            process.setNetPrice(netPrice);

            processRepository.save(process);

            // Recalculate Indicators immediately
            calculateIndicators(process.getId());

            logAudit("ACCEPT_OFFER", "Offer " + offerId + " ACCEPTED. Process advanced to PROMESSE_SIGNED.");
        } else if (status == OfferStatus.REJECTED) {
            logAudit("REJECT_OFFER", "Offer " + offerId + " REJECTED.");
        }

        return saved;
    }
    // --- FINANCIAL CALCULATIONS ---

    @Transactional
    public SaleProcess calculateIndicators(Long processId) {
        SaleProcess process = getProcess(processId);

        // 1. Acquisition Price (from Property)
        if (process.getAcquisitionPrice() == null && process.getPropertyId() != null) {
            Property property = propertyRepository.findById(process.getPropertyId()).orElse(null);
            if (property != null) {
                process.setAcquisitionPrice(property.getPrice());
            }
        }

        // 2. Total Works (from Construction)
        List<ConstructionProjectDTO> projects = constructionService.getProjectsByProperty(process.getPropertyId());
        BigDecimal totalWorks = projects.stream()
                .map(p -> p.getBudgetTotal() != null ? p.getBudgetTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        process.setTotalWorksAmount(totalWorks);

        // 3. Total Charges (from Finance)
        // Note: This includes ALL expenses (maintenance, tax, etc.)
        CashflowStats stats = financeService.getLifetimeStats(process.getPropertyId());
        process.setTotalChargesAmount(stats.getTotalExpense());

        // 4. Net Gain logic
        BigDecimal acquisition = process.getAcquisitionPrice() != null ? process.getAcquisitionPrice()
                : BigDecimal.ZERO;
        BigDecimal totalCost = acquisition.add(totalWorks).add(process.getTotalChargesAmount());

        BigDecimal exitPrice = BigDecimal.ZERO;
        if (process.getNetPrice() != null) {
            // Offer Accepted or Sold
            exitPrice = process.getNetPrice(); // Already Net of Agency Fees
        } else if (process.getAskingPrice() != null) {
            // Still on market, use asking price - estimated fees
            BigDecimal fees = process.getAgencyFee() != null ? process.getAgencyFee() : BigDecimal.ZERO;
            exitPrice = process.getAskingPrice().subtract(fees);
        }

        process.setEstimatedNetGain(exitPrice.subtract(totalCost));

        // 5. ROI
        if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
            // ROI = Net Gain / Total Cost
            BigDecimal roi = process.getEstimatedNetGain().divide(totalCost, 4, java.math.RoundingMode.HALF_UP);
            process.setGlobalRoi(roi);
        } else {
            process.setGlobalRoi(BigDecimal.ZERO);
        }

        return processRepository.save(process);
    }

    // --- DECISION SUPPORT ---

    public com.immopilot.modules.sales.service.dto.SaleRecommendationDTO getRecommendation(Long processId) {
        SaleProcess process = getProcess(processId);
        List<SaleVisit> visits = getVisits(processId);
        List<SaleOffer> offers = getOffers(processId);

        String action = "WAIT"; // Default
        String reason = "Le processus suit son cours.";
        String color = "blue"; // UI hint

        LocalDate start = process.getListingDate();
        if (start == null && process.getCreatedAt() != null) {
            start = process.getCreatedAt().toLocalDate();
        }
        if (start == null) {
            start = LocalDate.now();
        }

        long daysOnMarket = java.time.temporal.ChronoUnit.DAYS.between(start, java.time.LocalDate.now());

        BigDecimal bestOfferAmount = offers.stream()
                .map(SaleOffer::getOfferAmount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        boolean hasOffers = !offers.isEmpty();
        boolean hasGoodTraffic = visits.size() >= 5;

        // Logic Tree
        if (!hasOffers) {
            if (daysOnMarket > 60 && !hasGoodTraffic) {
                action = "ADJUST_PRICE";
                reason = "Plus de 60 jours sans offre et peu de visites. Envisagez une baisse de prix.";
                color = "red";
            } else if (hasGoodTraffic) {
                action = "WAIT";
                reason = "Bonne dynamique de visites. Une offre devrait arriver bientôt.";
                color = "blue";
            } else {
                action = "MARKETING";
                reason = "Augmentez la visibilité du bien (photos, annonces) pour attirer des visites.";
                color = "orange"; // fixed from amber
            }
        } else {
            // Has Offers
            BigDecimal target = process.getTargetPrice() != null ? process.getTargetPrice() : process.getAskingPrice();

            // Check if Best Offer is >= 95% of Target
            BigDecimal threshold = target.multiply(new BigDecimal("0.95"));

            if (bestOfferAmount.compareTo(threshold) >= 0) {
                action = "ACCEPT";
                reason = "Vous avez une offre excellente (> 95% du prix cible). Sécurisez la vente !";
                color = "green";
            } else {
                // Gap analysis
                action = "NEGOTIATE";
                reason = "Des offres sont là, mais en dessous de vos attentes. Négociez pour réduire l'écart.";
                color = "purple";
            }
        }

        // Override if finalized
        if (INACTIVE_STATUSES.contains(process.getStatus()) || process.getStatus() == SaleStatus.OFFER_ACCEPTED
                || process.getStatus() == SaleStatus.PROMESSE_SIGNED) {
            action = "CLOSED";
            reason = "Le processus est terminé ou verrouillé.";
            color = "gray";
        }

        return new com.immopilot.modules.sales.service.dto.SaleRecommendationDTO(action, reason, color);
    }

    private void logAudit(String action, String details) {
        // System user ID = 0 or 1 for now
        com.immopilot.modules.users.domain.AuditLog log = com.immopilot.modules.users.domain.AuditLog.builder()
                .userId(1L)
                .action(action)
                .details(details)
                .timestamp(java.time.Instant.now())
                .build();
        auditLogRepository.save(log);
    }
}
