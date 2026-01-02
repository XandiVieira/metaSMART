package com.relyon.metasmart.entity.subscription.dto;

import com.relyon.metasmart.entity.subscription.PurchaseType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCheckoutRequest {

    @NotNull(message = "Product type is required")
    private ProductType productType;

    private BillingPeriod billingPeriod;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    public enum ProductType {
        PREMIUM_SUBSCRIPTION,
        STREAK_SHIELD,
        STRUGGLING_ASSIST,
        GOAL_BOOST,
        GUARDIAN_SLOT
    }

    public enum BillingPeriod {
        MONTHLY,
        YEARLY
    }

    public PurchaseType toPurchaseType() {
        return switch (productType) {
            case STREAK_SHIELD -> PurchaseType.STREAK_SHIELD;
            case STRUGGLING_ASSIST -> PurchaseType.STRUGGLING_ASSIST;
            case GOAL_BOOST -> PurchaseType.GOAL_BOOST;
            case GUARDIAN_SLOT -> PurchaseType.GUARDIAN_SLOT;
            default -> null;
        };
    }
}
