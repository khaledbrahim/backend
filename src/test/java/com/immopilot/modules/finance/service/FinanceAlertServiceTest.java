package com.immopilot.modules.finance.service;

import com.immopilot.modules.finance.service.dto.CashflowStats;
import com.immopilot.modules.finance.service.dto.FinancialAlertDTO;
import com.immopilot.modules.properties.domain.Property;
import com.immopilot.modules.properties.repository.PropertyRepository;
import com.immopilot.modules.users.domain.User;
import com.immopilot.modules.users.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FinanceAlertServiceTest {

    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private FinanceService financeService;
    @Mock
    private UserService userService;

    @InjectMocks
    private FinanceAlertService alertService;

    private User user;
    private Property property;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test@example.com").build();
        property = Property.builder().id(100L).name("Test Property").user(user).build();
    }

    @Test
    void getAlerts_ShouldReturnNegativeCashflowAlert() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(propertyRepository.findByUserId(user.getId())).thenReturn(Collections.singletonList(property));

        // Mock negative cashflow for last 2 months
        CashflowStats negativeStats = CashflowStats.builder()
                .cashflow(new BigDecimal("-100"))
                .totalRevenue(BigDecimal.ZERO)
                .totalExpense(new BigDecimal("100"))
                .build();

        when(financeService.getStats(anyLong(), anyInt(), anyInt())).thenReturn(negativeStats);

        List<FinancialAlertDTO> alerts = alertService.getAlerts();

        assertEquals(2, alerts.size()); // Expect Negative Cashflow AND Expense > Revenue (since Expense (100) > Revenue
        // (0))
        assertTrue(alerts.stream().anyMatch(a -> a.getType().equals("NEGATIVE_CASHFLOW")));
        assertTrue(alerts.stream().anyMatch(a -> a.getType().equals("EXPENSE_WARNING")));
    }

    @Test
    void getAlerts_ShouldReturnNoAlerts_WhenCashflowPositive() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(propertyRepository.findByUserId(user.getId())).thenReturn(Collections.singletonList(property));

        CashflowStats positiveStats = CashflowStats.builder()
                .cashflow(new BigDecimal("100"))
                .totalRevenue(new BigDecimal("200"))
                .totalExpense(new BigDecimal("100"))
                .build();

        when(financeService.getStats(anyLong(), anyInt(), anyInt())).thenReturn(positiveStats);

        List<FinancialAlertDTO> alerts = alertService.getAlerts();

        assertEquals(0, alerts.size());
    }
}
