package com.immopilot.modules.users.web;

import com.immopilot.modules.users.domain.SubscriptionPlan;
import com.immopilot.modules.users.domain.UserSubscription;
import com.immopilot.modules.users.service.SubscriptionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/plans")
    public ResponseEntity<List<SubscriptionPlan>> getAllPlans() {
        return ResponseEntity.ok(subscriptionService.getAllPlans());
    }

    @GetMapping("/current")
    public ResponseEntity<UserSubscription> getCurrentSubscription() {
        return ResponseEntity.ok(subscriptionService.getCurrentSubscription());
    }

    @PostMapping("/change")
    public ResponseEntity<UserSubscription> changePlan(@RequestBody ChangePlanRequest request) {
        return ResponseEntity.ok(subscriptionService.changePlan(request.getPlanId(), request.getPaymentToken()));
    }

    @Data
    public static class ChangePlanRequest {
        private Integer planId;
        private String paymentToken;
    }
}
