package com.immopilot.modules.users.service;

import com.immopilot.modules.users.domain.AuditLog;
import com.immopilot.modules.users.domain.RegistrationStatus;
import com.immopilot.modules.users.domain.SubscriptionPlan;
import com.immopilot.modules.users.domain.SubscriptionStatus;
import com.immopilot.modules.users.domain.User;
import com.immopilot.modules.users.domain.UserSubscription;
import com.immopilot.modules.users.repository.AuditLogRepository;
import com.immopilot.modules.users.repository.SubscriptionPlanRepository;
import com.immopilot.modules.users.repository.UserRepository;
import com.immopilot.modules.users.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionPlanRepository planRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserService userService;

    public List<SubscriptionPlan> getAllPlans() {
        return planRepository.findAll();
    }

    public UserSubscription getCurrentSubscription() {
        User user = userService.getCurrentUser();
        return userSubscriptionRepository.findByUserId(user.getId())
                .orElse(null); // Or throw, or return default free
    }

    @Transactional
    public UserSubscription changePlan(Integer newPlanId, String paymentToken) {
        User user = userService.getCurrentUser();
        SubscriptionPlan newPlan = planRepository.findById(newPlanId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        UserSubscription currentSub = userSubscriptionRepository.findByUserId(user.getId())
                .orElse(null);

        // Logic for upgrade/downgrade
        boolean isUpgrade = currentSub == null
                || newPlan.getPriceMonthly().compareTo(currentSub.getPlan().getPriceMonthly()) > 0;

        if (isUpgrade && newPlan.getPriceMonthly().doubleValue() > 0) {
            // Mock payment validation
            if (paymentToken == null || paymentToken.isEmpty()) {
                throw new RuntimeException("Payment token required for upgrade");
            }
            // validatePayment(paymentToken);
        }

        if (currentSub == null) {
            currentSub = UserSubscription.builder()
                    .user(user)
                    .plan(newPlan)
                    .startDate(LocalDate.now())
                    .status(SubscriptionStatus.ACTIVE)
                    .autoRenew(true)
                    .build();
        } else {
            currentSub.setPlan(newPlan);
            // Reset dates if needed, or pro-rate. For MVP, just switch.
        }

        userSubscriptionRepository.save(currentSub);

        // Update user status if needed (e.g. SUBSCRIPTION_SELECTED -> VERIFIED)
        if (user.getRegistrationStatus() != RegistrationStatus.VERIFIED) {
            user.setRegistrationStatus(RegistrationStatus.VERIFIED);
            userRepository.save(user);
        }

        auditLogRepository.save(AuditLog.builder()
                .userId(user.getId())
                .action("CHANGE_PLAN")
                .details("Changed plan to " + newPlan.getName())
                .build());

        return currentSub;
    }
}
