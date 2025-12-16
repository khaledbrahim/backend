package com.immopilot.modules.users.repository;

import com.immopilot.modules.users.domain.SubscriptionStatus;
import com.immopilot.modules.users.domain.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    Optional<UserSubscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status);

    Optional<UserSubscription> findByUserId(Long userId);
}
