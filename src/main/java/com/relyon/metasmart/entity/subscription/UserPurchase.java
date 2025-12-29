package com.relyon.metasmart.entity.subscription;

import com.relyon.metasmart.entity.AuditableEntity;
import com.relyon.metasmart.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_purchases")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserPurchase extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PurchaseType purchaseType;

    @Builder.Default
    private Integer quantity = 1;

    @Builder.Default
    private Integer quantityRemaining = 1;

    private BigDecimal priceAmount;

    private String priceCurrency;

    private String externalTransactionId;

    private String paymentProvider;

    private LocalDateTime purchasedAt;

    private LocalDateTime expiresAt;

    public boolean hasRemaining() {
        return quantityRemaining != null && quantityRemaining > 0;
    }

    public void useOne() {
        if (hasRemaining()) {
            quantityRemaining--;
        }
    }
}
