package com.relyon.metasmart.repository;

import com.relyon.metasmart.entity.subscription.PurchaseType;
import com.relyon.metasmart.entity.subscription.UserPurchase;
import com.relyon.metasmart.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserPurchaseRepository extends JpaRepository<UserPurchase, Long> {

    @Query("SELECT up FROM UserPurchase up WHERE up.user = :user AND up.purchaseType = :type AND up.quantityRemaining > 0 AND (up.expiresAt IS NULL OR up.expiresAt > :now)")
    List<UserPurchase> findAvailablePurchases(@Param("user") User user, @Param("type") PurchaseType type, @Param("now") LocalDateTime now);

    @Query("SELECT COALESCE(SUM(up.quantityRemaining), 0) FROM UserPurchase up WHERE up.user = :user AND up.purchaseType = :type AND up.quantityRemaining > 0 AND (up.expiresAt IS NULL OR up.expiresAt > :now)")
    int countAvailable(@Param("user") User user, @Param("type") PurchaseType type, @Param("now") LocalDateTime now);

    List<UserPurchase> findByUserOrderByPurchasedAtDesc(User user);
}
