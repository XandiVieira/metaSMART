package com.relyon.metasmart.entity.subscription.dto;

import com.relyon.metasmart.entity.subscription.PurchaseType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseResponse {

    private Long id;
    private PurchaseType purchaseType;
    private Integer quantity;
    private Integer quantityRemaining;
    private BigDecimal priceAmount;
    private String priceCurrency;
    private LocalDateTime purchasedAt;
    private LocalDateTime expiresAt;
}
