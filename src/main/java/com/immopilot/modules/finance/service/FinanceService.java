package com.immopilot.modules.finance.service;

import com.immopilot.infrastructure.storage.StorageService;
import com.immopilot.modules.finance.domain.FinancialCategory;
import com.immopilot.modules.finance.domain.FinancialOperation;
import com.immopilot.modules.finance.domain.OperationType;
import com.immopilot.modules.finance.repository.FinancialCategoryRepository;
import com.immopilot.modules.finance.repository.FinancialOperationRepository;
import com.immopilot.modules.finance.service.dto.CashflowStats;
import com.immopilot.modules.finance.service.dto.FinancialCategoryDTO;
import com.immopilot.modules.finance.service.dto.OperationRequest;
import com.immopilot.modules.finance.service.dto.OperationResponse;
import com.immopilot.modules.properties.domain.Property;
import com.immopilot.modules.properties.domain.PropertyUnit;
import com.immopilot.modules.properties.repository.PropertyRepository;
import com.immopilot.modules.properties.repository.PropertyUnitRepository;
import com.immopilot.modules.users.domain.User;
import com.immopilot.modules.users.domain.UserSubscription;
import com.immopilot.modules.users.service.SubscriptionService;
import com.immopilot.modules.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final FinancialOperationRepository operationRepository;
    private final FinancialCategoryRepository categoryRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyUnitRepository unitRepository;
    private final StorageService storageService;
    private final SubscriptionService subscriptionService;
    private final UserService userService;

    @Transactional
    public OperationResponse createOperation(OperationRequest request, MultipartFile file) {
        User user = userService.getCurrentUser();
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new RuntimeException("Property not found"));

        if (!property.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to property");
        }

        checkSubscriptionLimits(user, request, file);

        FinancialCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
        }

        PropertyUnit unit = null;
        if (request.getUnitId() != null) {
            unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new RuntimeException("Unit not found"));
            if (!unit.getProperty().getId().equals(property.getId())) {
                throw new RuntimeException("Unit does not belong to property");
            }
        }

        String attachmentKey = null;
        String attachmentUrl = null;
        if (file != null && !file.isEmpty()) {
            attachmentKey = storageService.store(file);
            // In a real app, this might be a presigned URL or full path.
            // For now, we assume simple local storage service returns filename.
            attachmentUrl = "/api/files/" + attachmentKey;
        }

        FinancialOperation operation = FinancialOperation.builder()
                .property(property)
                .category(category)
                .unit(unit)
                .operationType(request.getOperationType())
                .amount(request.getAmount())
                .operationDate(request.getOperationDate())
                .description(request.getDescription())
                .source(request.getSource())
                .source(request.getSource())
                .currency(request.getCurrency())
                .status(request.getStatus())
                .sourceReference(request.getSourceReference())
                .attachmentStorageKey(attachmentKey)
                .attachmentUrl(attachmentUrl)
                .build();

        operation = operationRepository.save(operation);
        return mapToResponse(operation);
    }

    private void checkSubscriptionLimits(User user, OperationRequest request, MultipartFile file) {
        UserSubscription subscription = subscriptionService.getCurrentSubscription();
        String planName = subscription != null && subscription.getPlan() != null
                ? subscription.getPlan().getName()
                : "FREE";

        // Check Unit usage (Free plan: No Multi-Unit)
        if ("FREE".equals(planName) && request.getUnitId() != null) {
            throw new RuntimeException(
                    "Subscription limit: Unit management is not available on FREE plan. Please upgrade.");
        }

        // Check Operation Count (Free plan: Max 20 per property)
        if ("FREE".equals(planName)) {
            long count = operationRepository.countByPropertyId(request.getPropertyId());
            if (count >= 20) {
                throw new RuntimeException(
                        "Subscription limit reached: FREE plan allows max 20 operations per property.");
            }
        }

        // Check File Upload
        if (file != null && !file.isEmpty()) {
            if ("FREE".equals(planName)) {
                throw new RuntimeException("Subscription limit: file upload not allowed on FREE plan.");
            }
            long sizeInMb = file.getSize() / (1024 * 1024);
            if ("STANDARD".equals(planName) && sizeInMb > 25) {
                throw new RuntimeException("Subscription limit: file too large (max 25MB on STANDARD).");
            }
            if ("PRO".equals(planName) && sizeInMb > 100) {
                throw new RuntimeException("Subscription limit: file too large (max 100MB on PRO).");
            }
        }
    }

    @Transactional(readOnly = true)
    public List<OperationResponse> getOperations(Long propertyId, Long categoryId, OperationType type,
                                                 LocalDate startDate, LocalDate endDate) {
        User user = userService.getCurrentUser();

        if (propertyId != null) {
            Property property = propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new RuntimeException("Property not found"));
            if (!property.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Unauthorized");
            }
            return operationRepository.findWithFilters(propertyId, categoryId, type, startDate, endDate).stream()
                    .map(this::mapToResponse)
                    .toList();
        } else {
            // Global list for user
            return operationRepository.findAllByUserId(user.getId()).stream()
                    .filter(op -> (categoryId == null
                            || (op.getCategory() != null && op.getCategory().getId().equals(categoryId))))
                    .filter(op -> (type == null || op.getOperationType() == type))
                    .filter(op -> (startDate == null || !op.getOperationDate().isBefore(startDate)))
                    .filter(op -> (endDate == null || !op.getOperationDate().isAfter(endDate)))
                    .map(this::mapToResponse)
                    .toList();
        }
    }

    @Transactional(readOnly = true)
    public CashflowStats getStats(Long propertyId, Integer year, Integer month) {
        User user = userService.getCurrentUser();
        List<FinancialOperation> ops;

        LocalDate start = LocalDate.of(year != null ? year : LocalDate.now().getYear(),
                month != null ? month : 1, 1);
        LocalDate end;
        if (month != null) {
            end = start.plusMonths(1).minusDays(1);
        } else {
            end = LocalDate.of(year != null ? year : LocalDate.now().getYear(), 12, 31);
        }

        if (propertyId != null) {
            Property property = propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new RuntimeException("Property not found"));
            if (!property.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Unauthorized");
            }
            ops = operationRepository.findWithFilters(propertyId, null, null, start, end);
        } else {
            // Global stats
            // Ideally we'd have a repo method for this, doing in-memory filtering for now
            // or simple query
            ops = operationRepository.findAllByUserId(user.getId()).stream()
                    .filter(op -> !op.getOperationDate().isBefore(start) && !op.getOperationDate().isAfter(end))
                    .toList();
        }

        BigDecimal revenue = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        Map<String, BigDecimal> expenseByCategory = new HashMap<>();
        Map<String, BigDecimal> monthlyRevenue = new java.util.TreeMap<>(); // Sorted
        Map<String, BigDecimal> monthlyExpense = new java.util.TreeMap<>();
        Map<String, BigDecimal> monthlyCashflow = new java.util.TreeMap<>();

        for (FinancialOperation op : ops) {
            String monthKey = op.getOperationDate().toString().substring(0, 7); // yyyy-MM
            BigDecimal amount = op.getAmount();

            monthlyRevenue.putIfAbsent(monthKey, BigDecimal.ZERO);
            monthlyExpense.putIfAbsent(monthKey, BigDecimal.ZERO);

            if (op.getOperationType() == OperationType.REVENUE) {
                revenue = revenue.add(amount);
                monthlyRevenue.put(monthKey, monthlyRevenue.get(monthKey).add(amount));
            } else {
                expense = expense.add(amount);
                monthlyExpense.put(monthKey, monthlyExpense.get(monthKey).add(amount));

                String catName = op.getCategory() != null ? op.getCategory().getName() : "Uncategorized";
                expenseByCategory.merge(catName, amount, BigDecimal::add);
            }
        }

        // Calculate monthly cashflow
        java.util.Set<String> allMonths = new java.util.HashSet<>();
        allMonths.addAll(monthlyRevenue.keySet());
        allMonths.addAll(monthlyExpense.keySet());

        for (String m : allMonths) {
            monthlyRevenue.putIfAbsent(m, BigDecimal.ZERO);
            monthlyExpense.putIfAbsent(m, BigDecimal.ZERO);
            monthlyCashflow.put(m, monthlyRevenue.get(m).subtract(monthlyExpense.get(m)));
        }

        return CashflowStats.builder()
                .totalRevenue(revenue)
                .totalExpense(expense)
                .cashflow(revenue.subtract(expense))
                .expenseByCategory(expenseByCategory)
                .monthlyRevenue(monthlyRevenue)
                .monthlyExpense(monthlyExpense)
                .monthlyCashflow(monthlyCashflow)
                .build();
    }

    @Transactional
    public void deleteOperation(Long id) {
        User user = userService.getCurrentUser();
        FinancialOperation op = operationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Operation not found"));

        if (!op.getProperty().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        // Optionally delete file
        if (op.getAttachmentStorageKey() != null) {
            storageService.delete(op.getAttachmentStorageKey());
        }

        operationRepository.delete(op);
    }

    @Transactional
    public OperationResponse updateOperation(Long id, OperationRequest request) {
        User user = userService.getCurrentUser();
        FinancialOperation op = operationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Operation not found"));

        if (!op.getProperty().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        // Update fields
        op.setAmount(request.getAmount());
        op.setOperationType(request.getOperationType());
        op.setOperationDate(request.getOperationDate());
        op.setDescription(request.getDescription());

        if (request.getCategoryId() != null) {
            FinancialCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            op.setCategory(category);
        }

        if (request.getUnitId() != null) {
            PropertyUnit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new RuntimeException("Unit not found"));
            if (!unit.getProperty().getId().equals(op.getProperty().getId())) {
                throw new RuntimeException("Unit does not belong to property");
            }
            op.setUnit(unit);
        } else {
            op.setUnit(null);
        }

        op.setSource(request.getSource());
        op.setCurrency(request.getCurrency());
        op.setStatus(request.getStatus());
        op.setSourceReference(request.getSourceReference());

        return mapToResponse(operationRepository.save(op));
    }

    private OperationResponse mapToResponse(FinancialOperation op) {
        return OperationResponse.builder()
                .id(op.getId())
                .propertyId(op.getProperty().getId())
                .propertyName(op.getProperty().getName())
                .categoryId(op.getCategory() != null ? op.getCategory().getId() : null)
                .categoryName(op.getCategory() != null ? op.getCategory().getName() : null)
                .operationType(op.getOperationType())
                .amount(op.getAmount())
                .operationDate(op.getOperationDate())
                .description(op.getDescription())
                .attachmentUrl(op.getAttachmentUrl())
                .unitId(op.getUnit() != null ? op.getUnit().getId() : null)
                .unitName(op.getUnit() != null ? op.getUnit().getName() : null)
                .source(op.getSource())
                .currency(op.getCurrency())
                .status(op.getStatus())
                .sourceReference(op.getSourceReference())
                .createdAt(op.getCreatedAt())
                .updatedAt(op.getUpdatedAt())
                .build();
    }

    public List<FinancialCategoryDTO> getCategories() {
        User user = userService.getCurrentUser();
        return categoryRepository.findAllByUserIdOrSystemDefault(user.getId()).stream()
                .map(cat -> FinancialCategoryDTO.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .type(cat.getType())
                        .isSystemDefault(cat.isSystemDefault())
                        .applicablePropertyTypes(cat.getApplicablePropertyTypes())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public CashflowStats getLifetimeStats(Long propertyId) {
        User user = userService.getCurrentUser();
        List<FinancialOperation> ops;

        if (propertyId != null) {
            Property property = propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new RuntimeException("Property not found"));
            if (!property.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Unauthorized");
            }
            ops = operationRepository.findWithFilters(propertyId, null, null, null, null);
        } else {
            ops = operationRepository.findAllByUserId(user.getId());
        }

        BigDecimal revenue = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        Map<String, BigDecimal> expenseByCategory = new HashMap<>();

        for (FinancialOperation op : ops) {
            BigDecimal amount = op.getAmount();
            if (op.getOperationType() == OperationType.REVENUE) {
                revenue = revenue.add(amount);
            } else {
                expense = expense.add(amount);
                String catName = op.getCategory() != null ? op.getCategory().getName() : "Uncategorized";
                expenseByCategory.merge(catName, amount, BigDecimal::add);
            }
        }

        return CashflowStats.builder()
                .totalRevenue(revenue)
                .totalExpense(expense)
                .cashflow(revenue.subtract(expense))
                .expenseByCategory(expenseByCategory)
                .build();
    }
}
