package com.relyon.metasmart.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.relyon.metasmart.config.CorsConfig;
import com.relyon.metasmart.config.JwtService;
import com.relyon.metasmart.config.RateLimitConfig;
import com.relyon.metasmart.config.SecurityConfig;
import com.relyon.metasmart.entity.subscription.dto.CheckoutResponse;
import com.relyon.metasmart.entity.subscription.dto.CreateCheckoutRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.BadRequestException;
import com.relyon.metasmart.exception.GlobalExceptionHandler;
import com.relyon.metasmart.service.StripeService;
import com.stripe.model.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StripeController.class)
@Import({SecurityConfig.class, CorsConfig.class, RateLimitConfig.class, GlobalExceptionHandler.class})
class StripeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private StripeService stripeService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Nested
    @DisplayName("Create checkout session tests")
    class CreateCheckoutTests {

        @Test
        @DisplayName("Should create checkout session for premium subscription")
        void shouldCreateCheckoutSessionForPremiumSubscription() throws Exception {
            var user = User.builder()
                    .id(1L)
                    .email("test@example.com")
                    .name("Test User")
                    .build();

            var request = CreateCheckoutRequest.builder()
                    .productType(CreateCheckoutRequest.ProductType.PREMIUM_SUBSCRIPTION)
                    .billingPeriod(CreateCheckoutRequest.BillingPeriod.MONTHLY)
                    .build();

            var response = CheckoutResponse.builder()
                    .sessionId("cs_test_123")
                    .url("https://checkout.stripe.com/pay/cs_test_123")
                    .build();

            when(stripeService.createCheckoutSession(any(User.class), any(CreateCheckoutRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/payments/checkout")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sessionId").value("cs_test_123"))
                    .andExpect(jsonPath("$.url").value("https://checkout.stripe.com/pay/cs_test_123"));

            verify(stripeService).createCheckoutSession(any(User.class), any(CreateCheckoutRequest.class));
        }

        @Test
        @DisplayName("Should create checkout session for one-time purchase")
        void shouldCreateCheckoutSessionForOneTimePurchase() throws Exception {
            var user = User.builder()
                    .id(1L)
                    .email("test@example.com")
                    .name("Test User")
                    .build();

            var request = CreateCheckoutRequest.builder()
                    .productType(CreateCheckoutRequest.ProductType.STREAK_SHIELD)
                    .quantity(3)
                    .build();

            var response = CheckoutResponse.builder()
                    .sessionId("cs_test_456")
                    .url("https://checkout.stripe.com/pay/cs_test_456")
                    .build();

            when(stripeService.createCheckoutSession(any(User.class), any(CreateCheckoutRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/payments/checkout")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sessionId").value("cs_test_456"));
        }

        @Test
        @DisplayName("Should return 400 when product type is missing")
        void shouldReturn400WhenProductTypeMissing() throws Exception {
            var user = User.builder()
                    .id(1L)
                    .email("test@example.com")
                    .name("Test User")
                    .build();

            var request = "{}";

            mockMvc.perform(post("/api/v1/payments/checkout")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should deny access when not authenticated")
        void shouldDenyAccessWhenNotAuthenticated() throws Exception {
            var request = CreateCheckoutRequest.builder()
                    .productType(CreateCheckoutRequest.ProductType.PREMIUM_SUBSCRIPTION)
                    .build();

            mockMvc.perform(post("/api/v1/payments/checkout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 400 when Stripe is not configured")
        void shouldReturn400WhenStripeNotConfigured() throws Exception {
            var user = User.builder()
                    .id(1L)
                    .email("test@example.com")
                    .name("Test User")
                    .build();

            var request = CreateCheckoutRequest.builder()
                    .productType(CreateCheckoutRequest.ProductType.PREMIUM_SUBSCRIPTION)
                    .build();

            when(stripeService.createCheckoutSession(any(User.class), any(CreateCheckoutRequest.class)))
                    .thenThrow(new BadRequestException("Stripe is not configured"));

            mockMvc.perform(post("/api/v1/payments/checkout")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Webhook endpoint tests")
    class WebhookTests {

        @Test
        @DisplayName("Should handle valid webhook event")
        void shouldHandleValidWebhookEvent() throws Exception {
            var payload = "{\"type\":\"checkout.session.completed\"}";
            var sigHeader = "t=123,v1=abc";

            var event = mock(Event.class);
            when(stripeService.constructEvent(payload, sigHeader)).thenReturn(event);
            doNothing().when(stripeService).handleWebhookEvent(event);

            mockMvc.perform(post("/api/v1/payments/webhook")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Stripe-Signature", sigHeader)
                            .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Received"));

            verify(stripeService).constructEvent(payload, sigHeader);
            verify(stripeService).handleWebhookEvent(event);
        }

        @Test
        @DisplayName("Should return 400 for invalid signature")
        void shouldReturn400ForInvalidSignature() throws Exception {
            var payload = "{\"type\":\"test\"}";
            var sigHeader = "invalid-signature";

            when(stripeService.constructEvent(payload, sigHeader))
                    .thenThrow(new BadRequestException("Invalid webhook signature"));

            mockMvc.perform(post("/api/v1/payments/webhook")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Stripe-Signature", sigHeader)
                            .content(payload))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Webhook endpoint should be accessible without authentication")
        void webhookShouldBeAccessibleWithoutAuth() throws Exception {
            var payload = "{\"type\":\"test\"}";
            var sigHeader = "t=123,v1=abc";

            var event = mock(Event.class);
            when(stripeService.constructEvent(payload, sigHeader)).thenReturn(event);

            mockMvc.perform(post("/api/v1/payments/webhook")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Stripe-Signature", sigHeader)
                            .content(payload))
                    .andExpect(status().isOk());
        }
    }
}
