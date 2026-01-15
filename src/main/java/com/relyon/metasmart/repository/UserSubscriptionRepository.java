package com.relyon.metasmart.repository;

import com.relyon.metasmart.entity.subscription.SubscriptionStatus;
import com.relyon.metasmart.entity.subscription.UserSubscription;
import com.relyon.metasmart.entity.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    @Query("SELECT us FROM UserSubscription us WHERE us.user = :user AND us.status IN ('ACTIVE', 'TRIALING') ORDER BY us.createdAt DESC LIMIT 1")
    Optional<UserSubscription> findActiveSubscription(@Param("user") User user);

    Optional<UserSubscription> findFirstByUserOrderByCreatedAtDesc(User user);

    Optional<UserSubscription> findByExternalSubscriptionId(String externalSubscriptionId);

    boolean existsByUserAndStatusIn(User user, SubscriptionStatus... statuses);
}
