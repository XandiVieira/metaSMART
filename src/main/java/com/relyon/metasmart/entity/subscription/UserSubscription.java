package com.relyon.metasmart.entity.subscription;

import com.relyon.metasmart.entity.AuditableEntity;
import com.relyon.metasmart.entity.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "user_subscriptions")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscription extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SubscriptionTier tier = SubscriptionTier.FREE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private LocalDateTime trialEndDate;

    private LocalDateTime cancelledAt;

    private String externalSubscriptionId;

    private String paymentProvider;

    private BigDecimal priceAmount;

    private String priceCurrency;

    @Column(length = 50)
    private String billingPeriod;

    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.TRIALING;
    }

    public boolean isPremium() {
        return tier == SubscriptionTier.PREMIUM && isActive();
    }
}
