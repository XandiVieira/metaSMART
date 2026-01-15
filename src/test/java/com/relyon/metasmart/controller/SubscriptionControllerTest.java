package com.relyon.metasmart.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.relyon.metasmart.config.CorsConfig;
import com.relyon.metasmart.config.JwtService;
import com.relyon.metasmart.config.RateLimitConfig;
import com.relyon.metasmart.config.SecurityConfig;
import com.relyon.metasmart.entity.subscription.PurchaseType;
import com.relyon.metasmart.entity.subscription.SubscriptionStatus;
import com.relyon.metasmart.entity.subscription.SubscriptionTier;
import com.relyon.metasmart.entity.subscription.dto.PurchaseResponse;
import com.relyon.metasmart.entity.subscription.dto.SubscriptionResponse;
import com.relyon.metasmart.entity.subscription.dto.UserEntitlementsResponse;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.GlobalExceptionHandler;
import com.relyon.metasmart.service.SubscriptionService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SubscriptionController.class)
@Import({SecurityConfig.class, CorsConfig.class, RateLimitConfig.class, GlobalExceptionHandler.class})
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubscriptionService subscriptionService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private User user;
    private SubscriptionResponse subscriptionResponse;
    private UserEntitlementsResponse entitlementsResponse;
    private PurchaseResponse purchaseResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John")
                .email("john@test.com")
                .password("password")
                .build();

        subscriptionResponse = SubscriptionResponse.builder()
                .id(1L)
                .tier(SubscriptionTier.PREMIUM)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDateTime.now().minusDays(30))
                .endDate(LocalDateTime.now().plusDays(335))
                .billingPeriod("YEARLY")
                .isActive(true)
                .isPremium(true)
                .build();

        entitlementsResponse = UserEntitlementsResponse.builder()
                .tier(SubscriptionTier.PREMIUM)
                .isPremium(true)
                .maxActiveGoals(10)
                .maxGuardiansPerGoal(5)
                .progressHistoryDays(365)
                .streakShieldsPerMonth(3)
                .strugglingRequestsPerMonth(10)
                .streakShieldsAvailable(2)
                .strugglingAssistsAvailable(5)
                .goalBoostsAvailable(3)
                .guardianSlotsAvailable(2)
                .features(Map.of("advancedAnalytics", true, "prioritySupport", true))
                .build();

        purchaseResponse = PurchaseResponse.builder()
                .id(1L)
                .purchaseType(PurchaseType.STREAK_SHIELD)
                .quantity(3)
                .quantityRemaining(2)
                .priceAmount(BigDecimal.valueOf(2.99))
                .priceCurrency("USD")
                .purchasedAt(LocalDateTime.now().minusDays(7))
                .expiresAt(LocalDateTime.now().plusDays(23))
                .build();
    }

    @Nested
    @DisplayName("Subscription tests")
    class SubscriptionTests {

        @Test
        @DisplayName("Should get current subscription")
        void shouldGetCurrentSubscription() throws Exception {
            when(subscriptionService.getCurrentSubscription(any(User.class))).thenReturn(subscriptionResponse);

            mockMvc.perform(get("/api/v1/subscription")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.tier").value("PREMIUM"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.billingPeriod").value("YEARLY"))
                    .andExpect(jsonPath("$.isActive").value(true))
                    .andExpect(jsonPath("$.isPremium").value(true));
        }
    }

    @Nested
    @DisplayName("Entitlements tests")
    class EntitlementsTests {

        @Test
        @DisplayName("Should get user entitlements")
        void shouldGetUserEntitlements() throws Exception {
            when(subscriptionService.getEntitlements(any(User.class))).thenReturn(entitlementsResponse);

            mockMvc.perform(get("/api/v1/subscription/entitlements")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.tier").value("PREMIUM"))
                    .andExpect(jsonPath("$.isPremium").value(true))
                    .andExpect(jsonPath("$.maxActiveGoals").value(10))
                    .andExpect(jsonPath("$.maxGuardiansPerGoal").value(5))
                    .andExpect(jsonPath("$.streakShieldsAvailable").value(2))
                    .andExpect(jsonPath("$.strugglingAssistsAvailable").value(5))
                    .andExpect(jsonPath("$.goalBoostsAvailable").value(3));
        }
    }

    @Nested
    @DisplayName("Purchases tests")
    class PurchasesTests {

        @Test
        @DisplayName("Should get purchase history")
        void shouldGetPurchaseHistory() throws Exception {
            when(subscriptionService.getPurchases(any(User.class))).thenReturn(List.of(purchaseResponse));

            mockMvc.perform(get("/api/v1/subscription/purchases")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].purchaseType").value("STREAK_SHIELD"))
                    .andExpect(jsonPath("$[0].quantity").value(3))
                    .andExpect(jsonPath("$[0].quantityRemaining").value(2))
                    .andExpect(jsonPath("$[0].priceAmount").value(2.99))
                    .andExpect(jsonPath("$[0].priceCurrency").value("USD"));
        }

        @Test
        @DisplayName("Should return empty list when no purchases")
        void shouldReturnEmptyListWhenNoPurchases() throws Exception {
            when(subscriptionService.getPurchases(any(User.class))).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/subscription/purchases")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }
}