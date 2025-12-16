package com.immopilot.modules.finance.service;

import com.immopilot.infrastructure.storage.StorageService;
import com.immopilot.modules.finance.domain.FinancialOperation;
import com.immopilot.modules.finance.domain.OperationType;
import com.immopilot.modules.finance.repository.FinancialCategoryRepository;
import com.immopilot.modules.finance.repository.FinancialOperationRepository;
import com.immopilot.modules.finance.service.dto.OperationRequest;
import com.immopilot.modules.finance.service.dto.OperationResponse;
import com.immopilot.modules.properties.domain.Property;
import com.immopilot.modules.properties.domain.PropertyUnit;
import com.immopilot.modules.properties.repository.PropertyRepository;
import com.immopilot.modules.properties.repository.PropertyUnitRepository;
import com.immopilot.modules.users.domain.SubscriptionPlan;
import com.immopilot.modules.users.domain.User;
import com.immopilot.modules.users.domain.UserSubscription;
import com.immopilot.modules.users.service.SubscriptionService;
import com.immopilot.modules.users.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceServiceTest {

    @Mock
    private FinancialOperationRepository operationRepository;
    @Mock
    private FinancialCategoryRepository categoryRepository;
    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private PropertyUnitRepository unitRepository;
    @Mock
    private StorageService storageService;
    @Mock
    private SubscriptionService subscriptionService;
    @Mock
    private UserService userService;

    @InjectMocks
    private FinanceService financeService;

    private User user;
    private Property property;
    private PropertyUnit unit;
    private OperationRequest request;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test@example.com").build();
        property = Property.builder().id(10L).user(user).name("Test Property").build();
        unit = PropertyUnit.builder().id(100L).property(property).name("Unit 1").build();

        request = OperationRequest.builder()
                .propertyId(10L)
                .amount(new BigDecimal("100.00"))
                .operationType(OperationType.EXPENSE)
                .operationDate(LocalDate.now())
                .build();
    }

    @Test
    void createOperation_ShouldSucceed_WhenValid() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
        // Mock Free Plan by default (SubscriptionService returns null or FREE)
        mockSubscription("FREE");
        when(operationRepository.countByPropertyId(10L)).thenReturn(5L); // Below limit
        when(operationRepository.save(any(FinancialOperation.class))).thenAnswer(i -> {
            FinancialOperation op = i.getArgument(0);
            op.setId(500L);
            return op;
        });

        OperationResponse result = financeService.createOperation(request, null);

        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), result.getAmount());
        verify(operationRepository).save(any(FinancialOperation.class));
    }

    @Test
    void createOperation_ShouldSucceed_WithUnitAndProPlan() {
        mockSubscription("PRO");
        when(userService.getCurrentUser()).thenReturn(user);
        when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
        when(unitRepository.findById(100L)).thenReturn(Optional.of(unit));
        when(operationRepository.save(any(FinancialOperation.class))).thenAnswer(i -> {
            FinancialOperation op = i.getArgument(0);
            op.setId(501L);
            return op;
        });

        request.setUnitId(100L);
        OperationResponse result = financeService.createOperation(request, null);

        assertNotNull(result);
        assertEquals(100L, result.getUnitId());
    }

    @Test
    void createOperation_ShouldThrow_WhenFreePlanUsesUnit() {
        mockSubscription("FREE");
        when(userService.getCurrentUser()).thenReturn(user);
        when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));

        request.setUnitId(100L); // Associating unit on FREE plan matches restriction

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> financeService.createOperation(request, null));
        assertTrue(exception.getMessage().contains("Unit management is not available on FREE plan"));
    }

    @Test
    void createOperation_ShouldThrow_WhenFreePlanLimitReached() {
        mockSubscription("FREE");
        when(userService.getCurrentUser()).thenReturn(user);
        when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
        when(operationRepository.countByPropertyId(10L)).thenReturn(20L); // Limit reached

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> financeService.createOperation(request, null));
        assertTrue(exception.getMessage().contains("Subscription limit reached"));
    }

    @Test
    void createOperation_ShouldThrow_WhenUnitDoesNotBelongToProperty() {
        mockSubscription("PRO");
        when(userService.getCurrentUser()).thenReturn(user);
        when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));

        Property otherProperty = Property.builder().id(20L).build();
        PropertyUnit otherUnit = PropertyUnit.builder().id(200L).property(otherProperty).build();
        when(unitRepository.findById(200L)).thenReturn(Optional.of(otherUnit));

        request.setUnitId(200L);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> financeService.createOperation(request, null));
        assertTrue(exception.getMessage().contains("Unit does not belong to property"));
    }

    @Test
    void createOperation_ShouldThrow_WhenUnauthorizedProperty() {
        User otherUser = User.builder().id(2L).build();
        when(userService.getCurrentUser()).thenReturn(otherUser);
        when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));

        assertThrows(RuntimeException.class, () -> financeService.createOperation(request, null));
    }

    private void mockSubscription(String planName) {
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setName(planName);
        UserSubscription sub = new UserSubscription();
        sub.setPlan(plan);
        when(subscriptionService.getCurrentSubscription()).thenReturn(sub);
    }
}
