package com.relyon.metasmart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.relyon.metasmart.config.StripeConfig;
import com.relyon.metasmart.entity.subscription.PurchaseType;
import com.relyon.metasmart.entity.subscription.SubscriptionStatus;
import com.relyon.metasmart.entity.subscription.UserPurchase;
import com.relyon.metasmart.entity.subscription.UserSubscription;
import com.relyon.metasmart.entity.subscription.dto.CreateCheckoutRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.BadRequestException;
import com.relyon.metasmart.repository.UserPurchaseRepository;
import com.relyon.metasmart.repository.UserSubscriptionRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StripeServiceTest {

    @Mock
    private StripeConfig stripeConfig;

    @Mock
    private UserSubscriptionRepository subscriptionRepository;

    @Mock
    private UserPurchaseRepository purchaseRepository;

    @Mock
    private GoalLockService goalLockService;

    @InjectMocks
    private StripeService stripeService;

    private User user;
    private StripeConfig.Prices prices;

    @BeforeEach
    void setUp() throws Exception {
        user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        prices = new StripeConfig.Prices();
        prices.setPremiumMonthly("price_premium_monthly");
        prices.setPremiumYearly("price_premium_yearly");
        prices.setStreakShield("price_streak_shield");
        prices.setStrugglingAssist("price_struggling_assist");
        prices.setGoalBoost("price_goal_boost");
        prices.setGuardianSlot("price_guardian_slot");

        setPrivateField(stripeService, "frontendUrl", "http://localhost:3000");
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Nested
    @DisplayName("Create checkout session tests")
    class CreateCheckoutSessionTests {

        @Test
        @DisplayName("Should throw BadRequestException when Stripe not configured")
        void shouldThrowWhenStripeNotConfigured() {
            var request = CreateCheckoutRequest.builder()
                    .productType(CreateCheckoutRequest.ProductType.PREMIUM_SUBSCRIPTION)
                    .build();

            when(stripeConfig.getApiKey()).thenReturn(null);

            assertThatThrownBy(() -> stripeService.createCheckoutSession(user, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Stripe is not configured");
        }

        @Test
        @DisplayName("Should throw BadRequestException when Stripe API key is blank")
        void shouldThrowWhenStripeApiKeyBlank() {
            var request = CreateCheckoutRequest.builder()
                    .productType(CreateCheckoutRequest.ProductType.PREMIUM_SUBSCRIPTION)
                    .build();

            when(stripeConfig.getApiKey()).thenReturn("   ");

            assertThatThrownBy(() -> stripeService.createCheckoutSession(user, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Stripe is not configured");
        }

        @Test
        @DisplayName("Should create subscription checkout session for monthly premium")
        void shouldCreateSubscriptionCheckoutForMonthlyPremium() throws StripeException {
            var request = CreateCheckoutRequest.builder()
                    .productType(CreateCheckoutRequest.ProductType.PREMIUM_SUBSCRIPTION)
                    .billingPeriod(CreateCheckoutRequest.BillingPeriod.MONTHLY)
                    .build();

            when(stripeConfig.getApiKey()).thenReturn("sk_test_xxx");
            when(stripeConfig.getPrices()).thenReturn(prices);

            try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {
                var mockSession = mock(Session.class);
                when(mockSession.getId()).thenReturn("cs_test_123");
                when(mockSession.getUrl()).thenReturn("https://checkout.stripe.com/pay/cs_test_123");
                sessionMock.when(() -> Session.create(any(SessionCreateParams.class))).thenReturn(mockSession);

                var response = stripeService.createCheckoutSession(user, request);

                assertThat(response).isNotNull();
                assertThat(response.getSessionId()).isEqualTo("cs_test_123");
                assertThat(response.getUrl()).isEqualTo("https://checkout.stripe.com/pay/cs_test_123");
            }
        }

        @Test
        @DisplayName("Should create subscription checkout session for yearly premium")
        void shouldCreateSubscriptionCheckoutForYearlyPremium() throws StripeException {
            var request = CreateCheckoutRequest.builder()
                    .productType(CreateCheckoutRequest.ProductType.PREMIUM_SUBSCRIPTION)
                    .billingPeriod(CreateCheckoutRequest.BillingPeriod.YEARLY)
                    .build();

            when(stripeConfig.getApiKey()).thenReturn("sk_test_xxx");
            when(stripeConfig.getPrices()).thenReturn(prices);

            try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {
                var mockSession = mock(Session.class);
                when(mockSession.getId()).thenReturn("cs_test_yearly");
                when(mockSession.getUrl()).thenReturn("https://checkout.stripe.com/pay/cs_test_yearly");
                sessionMock.when(() -> Session.create(any(SessionCreateParams.class))).thenReturn(mockSession);

                var response = stripeService.createCheckoutSession(user, request);

                assertThat(response).isNotNull();
                assertThat(response.getSessionId()).isEqualTo("cs_test_yearly");
            }
        }

        @Test
        @DisplayName("Should create payment checkout session for streak shield")
        void shouldCreatePaymentCheckoutForStreakShield() throws StripeException {
            var request = CreateCheckoutRequest.builder()
                    .productType(CreateCheckoutRequest.ProductType.STREAK_SHIELD)
                    .quantity(3)
                    .build();

            when(stripeConfig.getApiKey()).thenReturn("sk_test_xxx");
            when(stripeConfig.getPrices()).thenReturn(prices);

            try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {
                var mockSession = mock(Session.class);
                when(mockSession.getId()).thenReturn("cs_test_shield");
                when(mockSession.getUrl()).thenReturn("https://checkout.stripe.com/pay/cs_test_shield");
                sessionMock.when(() -> Session.create(any(SessionCreateParams.class))).thenReturn(mockSession);

                var response = stripeService.createCheckoutSession(user, request);

                assertThat(response).isNotNull();
                assertThat(response.getSessionId()).isEqualTo("cs_test_shield");
            }
        }

        @Test
        @DisplayName("Should create payment checkout for struggling assist")
        void shouldCreatePaymentCheckoutForStrugglingAssist() throws StripeException {
            var request = CreateCheckoutRequest.builder()
                    .productType(CreateCheckoutRequest.ProductType.STRUGGLING_ASSIST)
                    .build();

            when(stripeConfig.getApiKey()).thenReturn("sk_test_xxx");
            when(stripeConfig.getPrices()).thenReturn(prices);

            try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {
                var mockSession = mock(Session.class);
                when(mockSession.getId()).thenReturn("cs_test_assist");
                when(mockSession.getUrl()).thenReturn("https://checkout.stripe.com/pay/cs_test_assist");
                sessionMock.when(() -> Session.create(any(SessionCreateParams.class))).thenReturn(mockSession);

                var response = stripeService.createCheckoutSession(user, request);

                assertThat(response).isNotNull();
            }
        }

        @Test
        @DisplayName("Should create payment checkout for goal boost")
        void shouldCreatePaymentCheckoutForGoalBoost() throws StripeException {
            var request = CreateCheckoutRequest.builder()
                    .productType(CreateCheckoutRequest.ProductType.GOAL_BOOST)
                    .build();

            when(stripeConfig.getApiKey()).thenReturn("sk_test_xxx");
            when(stripeConfig.getPrices()).thenReturn(prices);

            try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {
                var mockSession = mock(Session.class);
                when(mockSession.getId()).thenReturn("cs_test_boost");
                when(mockSession.getUrl()).thenReturn("https://checkout.stripe.com/pay/cs_test_boost");
                sessionMock.when(() -> Session.create(any(SessionCreateParams.class))).thenReturn(mockSession);

                var response = stripeService.createCheckoutSession(user, request);

                assertThat(response).isNotNull();
            }
        }

        @Test
        @DisplayName("Should create payment checkout for guardian slot")
        void shouldCreatePaymentCheckoutForGuardianSlot() throws StripeException {
            var request = CreateCheckoutRequest.builder()
                    .productType(CreateCheckoutRequest.ProductType.GUARDIAN_SLOT)
                    .build();

            when(stripeConfig.getApiKey()).thenReturn("sk_test_xxx");
            when(stripeConfig.getPrices()).thenReturn(prices);

            try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {
                var mockSession = mock(Session.class);
                when(mockSession.getId()).thenReturn("cs_test_guardian");
                when(mockSession.getUrl()).thenReturn("https://checkout.stripe.com/pay/cs_test_guardian");
                sessionMock.when(() -> Session.create(any(SessionCreateParams.class))).thenReturn(mockSession);

                var response = stripeService.createCheckoutSession(user, request);

                assertThat(response).isNotNull();
            }
        }

        @Test
        @DisplayName("Should default quantity to 1 when not provided")
        void shouldDefaultQuantityToOne() throws StripeException {
            var request = CreateCheckoutRequest.builder()
                    .productType(CreateCheckoutRequest.ProductType.STREAK_SHIELD)
                    .quantity(null)
                    .build();

            when(stripeConfig.getApiKey()).thenReturn("sk_test_xxx");
            when(stripeConfig.getPrices()).thenReturn(prices);

            try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {
                var mockSession = mock(Session.class);
                when(mockSession.getId()).thenReturn("cs_test_default_qty");
                when(mockSession.getUrl()).thenReturn("https://checkout.stripe.com/pay/cs_test_default_qty");
                sessionMock.when(() -> Session.create(any(SessionCreateParams.class))).thenReturn(mockSession);

                var response = stripeService.createCheckoutSession(user, request);

                assertThat(response).isNotNull();
            }
        }

        @Test
        @DisplayName("Should throw RuntimeException when Stripe API fails")
        void shouldThrowRuntimeExceptionWhenStripeApiFails() {
            var request = CreateCheckoutRequest.builder()
                    .productType(CreateCheckoutRequest.ProductType.PREMIUM_SUBSCRIPTION)
                    .build();

            when(stripeConfig.getApiKey()).thenReturn("sk_test_xxx");
            when(stripeConfig.getPrices()).thenReturn(prices);

            try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {
                sessionMock.when(() -> Session.create(any(SessionCreateParams.class)))
                        .thenThrow(new RuntimeException("Stripe API error"));

                assertThatThrownBy(() -> stripeService.createCheckoutSession(user, request))
                        .isInstanceOf(RuntimeException.class);
            }
        }
    }

    @Nested
    @DisplayName("Construct event tests")
    class ConstructEventTests {

        @Test
        @DisplayName("Should construct event successfully")
        void shouldConstructEventSuccessfully() {
            var payload = "{\"type\":\"checkout.session.completed\"}";
            var sigHeader = "t=123,v1=abc";

            when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test");

            try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
                var mockEvent = mock(Event.class);
                webhookMock.when(() -> Webhook.constructEvent(payload, sigHeader, "whsec_test"))
                        .thenReturn(mockEvent);

                var event = stripeService.constructEvent(payload, sigHeader);

                assertThat(event).isNotNull();
            }
        }

        @Test
        @DisplayName("Should throw BadRequestException for invalid signature")
        void shouldThrowBadRequestExceptionForInvalidSignature() {
            var payload = "{\"type\":\"checkout.session.completed\"}";
            var sigHeader = "invalid";

            when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test");

            try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
                webhookMock.when(() -> Webhook.constructEvent(payload, sigHeader, "whsec_test"))
                        .thenThrow(new SignatureVerificationException("Invalid signature", sigHeader));

                assertThatThrownBy(() -> stripeService.constructEvent(payload, sigHeader))
                        .isInstanceOf(BadRequestException.class)
                        .hasMessage("Invalid webhook signature");
            }
        }
    }

    @Nested
    @DisplayName("Handle webhook event tests")
    class HandleWebhookEventTests {

        @Test
        @DisplayName("Should handle checkout.session.completed for one-time purchase")
        void shouldHandleCheckoutCompletedForOneTimePurchase() {
            var event = mock(Event.class);
            var session = mock(Session.class);
            var deserializer = mock(EventDataObjectDeserializer.class);

            when(event.getType()).thenReturn("checkout.session.completed");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);
            when(deserializer.getObject()).thenReturn(Optional.of(session));
            when(session.getMetadata()).thenReturn(Map.of(
                    "user_id", "1",
                    "product_type", "STREAK_SHIELD",
                    "quantity", "2"
            ));
            when(session.getAmountTotal()).thenReturn(999L);
            when(session.getCurrency()).thenReturn("usd");
            when(session.getPaymentIntent()).thenReturn("pi_test_123");

            stripeService.handleWebhookEvent(event);

            var captor = ArgumentCaptor.forClass(UserPurchase.class);
            verify(purchaseRepository).save(captor.capture());

            var savedPurchase = captor.getValue();
            assertThat(savedPurchase.getPurchaseType()).isEqualTo(PurchaseType.STREAK_SHIELD);
            assertThat(savedPurchase.getQuantity()).isEqualTo(2);
            assertThat(savedPurchase.getQuantityRemaining()).isEqualTo(2);
            assertThat(savedPurchase.getPriceAmount()).isEqualTo(BigDecimal.valueOf(9.99));
            assertThat(savedPurchase.getPriceCurrency()).isEqualTo("usd");
        }

        @Test
        @DisplayName("Should handle checkout.session.completed for subscription")
        void shouldHandleCheckoutCompletedForSubscription() {
            var event = mock(Event.class);
            var session = mock(Session.class);
            var deserializer = mock(EventDataObjectDeserializer.class);

            when(event.getType()).thenReturn("checkout.session.completed");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);
            when(deserializer.getObject()).thenReturn(Optional.of(session));
            when(session.getMetadata()).thenReturn(Map.of(
                    "user_id", "1",
                    "product_type", "PREMIUM_SUBSCRIPTION",
                    "quantity", "1"
            ));

            stripeService.handleWebhookEvent(event);

            verify(purchaseRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should handle checkout.session.completed for struggling assist")
        void shouldHandleCheckoutCompletedForStrugglingAssist() {
            var event = mock(Event.class);
            var session = mock(Session.class);
            var deserializer = mock(EventDataObjectDeserializer.class);

            when(event.getType()).thenReturn("checkout.session.completed");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);
            when(deserializer.getObject()).thenReturn(Optional.of(session));
            when(session.getMetadata()).thenReturn(Map.of(
                    "user_id", "1",
                    "product_type", "STRUGGLING_ASSIST",
                    "quantity", "1"
            ));
            when(session.getAmountTotal()).thenReturn(499L);
            when(session.getCurrency()).thenReturn("usd");
            when(session.getPaymentIntent()).thenReturn("pi_test_456");

            stripeService.handleWebhookEvent(event);

            var captor = ArgumentCaptor.forClass(UserPurchase.class);
            verify(purchaseRepository).save(captor.capture());
            assertThat(captor.getValue().getPurchaseType()).isEqualTo(PurchaseType.STRUGGLING_ASSIST);
        }

        @Test
        @DisplayName("Should handle checkout.session.completed for goal boost")
        void shouldHandleCheckoutCompletedForGoalBoost() {
            var event = mock(Event.class);
            var session = mock(Session.class);
            var deserializer = mock(EventDataObjectDeserializer.class);

            when(event.getType()).thenReturn("checkout.session.completed");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);
            when(deserializer.getObject()).thenReturn(Optional.of(session));
            when(session.getMetadata()).thenReturn(Map.of(
                    "user_id", "1",
                    "product_type", "GOAL_BOOST",
                    "quantity", "1"
            ));
            when(session.getAmountTotal()).thenReturn(299L);
            when(session.getCurrency()).thenReturn("usd");
            when(session.getPaymentIntent()).thenReturn("pi_test_789");

            stripeService.handleWebhookEvent(event);

            var captor = ArgumentCaptor.forClass(UserPurchase.class);
            verify(purchaseRepository).save(captor.capture());
            assertThat(captor.getValue().getPurchaseType()).isEqualTo(PurchaseType.GOAL_BOOST);
        }

        @Test
        @DisplayName("Should handle checkout.session.completed for guardian slot")
        void shouldHandleCheckoutCompletedForGuardianSlot() {
            var event = mock(Event.class);
            var session = mock(Session.class);
            var deserializer = mock(EventDataObjectDeserializer.class);

            when(event.getType()).thenReturn("checkout.session.completed");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);
            when(deserializer.getObject()).thenReturn(Optional.of(session));
            when(session.getMetadata()).thenReturn(Map.of(
                    "user_id", "1",
                    "product_type", "GUARDIAN_SLOT",
                    "quantity", "1"
            ));
            when(session.getAmountTotal()).thenReturn(199L);
            when(session.getCurrency()).thenReturn("usd");
            when(session.getPaymentIntent()).thenReturn("pi_test_guardian");

            stripeService.handleWebhookEvent(event);

            var captor = ArgumentCaptor.forClass(UserPurchase.class);
            verify(purchaseRepository).save(captor.capture());
            assertThat(captor.getValue().getPurchaseType()).isEqualTo(PurchaseType.GUARDIAN_SLOT);
        }

        @Test
        @DisplayName("Should handle checkout with session id when payment intent is null")
        void shouldHandleCheckoutWithSessionIdWhenPaymentIntentNull() {
            var event = mock(Event.class);
            var session = mock(Session.class);
            var deserializer = mock(EventDataObjectDeserializer.class);

            when(event.getType()).thenReturn("checkout.session.completed");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);
            when(deserializer.getObject()).thenReturn(Optional.of(session));
            when(session.getMetadata()).thenReturn(Map.of(
                    "user_id", "1",
                    "product_type", "STREAK_SHIELD",
                    "quantity", "1"
            ));
            when(session.getAmountTotal()).thenReturn(null);
            when(session.getCurrency()).thenReturn("usd");
            when(session.getPaymentIntent()).thenReturn(null);
            when(session.getId()).thenReturn("cs_test_session");

            stripeService.handleWebhookEvent(event);

            var captor = ArgumentCaptor.forClass(UserPurchase.class);
            verify(purchaseRepository).save(captor.capture());
            assertThat(captor.getValue().getExternalTransactionId()).isEqualTo("cs_test_session");
            assertThat(captor.getValue().getPriceAmount()).isNull();
        }

        @Test
        @DisplayName("Should handle default quantity when metadata missing")
        void shouldHandleDefaultQuantityWhenMetadataMissing() {
            var event = mock(Event.class);
            var session = mock(Session.class);
            var deserializer = mock(EventDataObjectDeserializer.class);

            when(event.getType()).thenReturn("checkout.session.completed");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);
            when(deserializer.getObject()).thenReturn(Optional.of(session));
            when(session.getMetadata()).thenReturn(Map.of(
                    "user_id", "1",
                    "product_type", "STREAK_SHIELD"
            ));
            when(session.getAmountTotal()).thenReturn(499L);
            when(session.getCurrency()).thenReturn("usd");
            when(session.getPaymentIntent()).thenReturn("pi_test");

            stripeService.handleWebhookEvent(event);

            var captor = ArgumentCaptor.forClass(UserPurchase.class);
            verify(purchaseRepository).save(captor.capture());
            assertThat(captor.getValue().getQuantity()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle customer.subscription.created")
        void shouldHandleSubscriptionCreated() {
            var event = mock(Event.class);
            var subscription = mock(Subscription.class);
            var deserializer = mock(EventDataObjectDeserializer.class);
            var items = mock(SubscriptionItemCollection.class);
            var subscriptionItem = mock(SubscriptionItem.class);
            var price = mock(Price.class);
            var recurring = mock(Price.Recurring.class);

            when(event.getType()).thenReturn("customer.subscription.created");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);
            when(deserializer.getObject()).thenReturn(Optional.of(subscription));
            when(subscription.getMetadata()).thenReturn(Map.of("user_id", "1"));
            when(subscription.getId()).thenReturn("sub_test_123");
            when(subscription.getStatus()).thenReturn("active");
            when(subscription.getStartDate()).thenReturn(1704067200L);
            when(subscription.getCurrentPeriodEnd()).thenReturn(1706745600L);
            when(subscription.getTrialEnd()).thenReturn(null);
            when(subscription.getCanceledAt()).thenReturn(null);
            when(subscription.getItems()).thenReturn(items);
            when(items.getData()).thenReturn(List.of(subscriptionItem));
            when(subscriptionItem.getPrice()).thenReturn(price);
            when(price.getUnitAmount()).thenReturn(999L);
            when(price.getCurrency()).thenReturn("usd");
            when(price.getRecurring()).thenReturn(recurring);
            when(recurring.getInterval()).thenReturn("month");

            when(subscriptionRepository.findByExternalSubscriptionId("sub_test_123")).thenReturn(Optional.empty());

            stripeService.handleWebhookEvent(event);

            var captor = ArgumentCaptor.forClass(UserSubscription.class);
            verify(subscriptionRepository).save(captor.capture());
            assertThat(captor.getValue().getExternalSubscriptionId()).isEqualTo("sub_test_123");
            assertThat(captor.getValue().getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should handle customer.subscription.created with no user id in metadata")
        void shouldHandleSubscriptionCreatedWithNoUserId() {
            var event = mock(Event.class);
            var subscription = mock(Subscription.class);
            var deserializer = mock(EventDataObjectDeserializer.class);

            when(event.getType()).thenReturn("customer.subscription.created");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);
            when(deserializer.getObject()).thenReturn(Optional.of(subscription));
            when(subscription.getMetadata()).thenReturn(Map.of());
            when(subscription.getId()).thenReturn("sub_test_no_user");

            stripeService.handleWebhookEvent(event);

            verify(subscriptionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should handle customer.subscription.updated")
        void shouldHandleSubscriptionUpdated() {
            var event = mock(Event.class);
            var subscription = mock(Subscription.class);
            var deserializer = mock(EventDataObjectDeserializer.class);
            var existingSubscription = UserSubscription.builder()
                    .id(1L)
                    .externalSubscriptionId("sub_test_123")
                    .status(SubscriptionStatus.ACTIVE)
                    .build();
            var items = mock(SubscriptionItemCollection.class);
            var subscriptionItem = mock(SubscriptionItem.class);
            var price = mock(Price.class);

            when(event.getType()).thenReturn("customer.subscription.updated");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);
            when(deserializer.getObject()).thenReturn(Optional.of(subscription));
            when(subscription.getMetadata()).thenReturn(Map.of("user_id", "1"));
            when(subscription.getId()).thenReturn("sub_test_123");
            when(subscription.getStatus()).thenReturn("past_due");
            when(subscription.getStartDate()).thenReturn(1704067200L);
            when(subscription.getCurrentPeriodEnd()).thenReturn(1706745600L);
            when(subscription.getTrialEnd()).thenReturn(null);
            when(subscription.getCanceledAt()).thenReturn(null);
            when(subscription.getItems()).thenReturn(items);
            when(items.getData()).thenReturn(List.of(subscriptionItem));
            when(subscriptionItem.getPrice()).thenReturn(price);
            when(price.getUnitAmount()).thenReturn(999L);
            when(price.getCurrency()).thenReturn("usd");
            when(price.getRecurring()).thenReturn(null);

            when(subscriptionRepository.findByExternalSubscriptionId("sub_test_123"))
                    .thenReturn(Optional.of(existingSubscription));

            stripeService.handleWebhookEvent(event);

            var captor = ArgumentCaptor.forClass(UserSubscription.class);
            verify(subscriptionRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(SubscriptionStatus.PAST_DUE);
        }

        @Test
        @DisplayName("Should handle customer.subscription.updated with trial end")
        void shouldHandleSubscriptionUpdatedWithTrialEnd() {
            var event = mock(Event.class);
            var subscription = mock(Subscription.class);
            var deserializer = mock(EventDataObjectDeserializer.class);
            var items = mock(SubscriptionItemCollection.class);
            var subscriptionItem = mock(SubscriptionItem.class);
            var price = mock(Price.class);

            when(event.getType()).thenReturn("customer.subscription.updated");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);
            when(deserializer.getObject()).thenReturn(Optional.of(subscription));
            when(subscription.getMetadata()).thenReturn(Map.of("user_id", "1"));
            when(subscription.getId()).thenReturn("sub_test_trial");
            when(subscription.getStatus()).thenReturn("trialing");
            when(subscription.getStartDate()).thenReturn(1704067200L);
            when(subscription.getCurrentPeriodEnd()).thenReturn(1706745600L);
            when(subscription.getTrialEnd()).thenReturn(1705276800L);
            when(subscription.getCanceledAt()).thenReturn(null);
            when(subscription.getItems()).thenReturn(items);
            when(items.getData()).thenReturn(List.of(subscriptionItem));
            when(subscriptionItem.getPrice()).thenReturn(price);
            when(price.getUnitAmount()).thenReturn(999L);
            when(price.getCurrency()).thenReturn("usd");
            when(price.getRecurring()).thenReturn(null);

            when(subscriptionRepository.findByExternalSubscriptionId("sub_test_trial")).thenReturn(Optional.empty());

            stripeService.handleWebhookEvent(event);

            var captor = ArgumentCaptor.forClass(UserSubscription.class);
            verify(subscriptionRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(SubscriptionStatus.TRIALING);
            assertThat(captor.getValue().getTrialEndDate()).isNotNull();
        }

        @Test
        @DisplayName("Should handle customer.subscription.updated with canceled at")
        void shouldHandleSubscriptionUpdatedWithCanceledAt() {
            var event = mock(Event.class);
            var subscription = mock(Subscription.class);
            var deserializer = mock(EventDataObjectDeserializer.class);
            var items = mock(SubscriptionItemCollection.class);
            var subscriptionItem = mock(SubscriptionItem.class);
            var price = mock(Price.class);

            when(event.getType()).thenReturn("customer.subscription.updated");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);
            when(deserializer.getObject()).thenReturn(Optional.of(subscription));
            when(subscription.getMetadata()).thenReturn(Map.of("user_id", "1"));
            when(subscription.getId()).thenReturn("sub_test_canceled");
            when(subscription.getStatus()).thenReturn("canceled");
            when(subscription.getStartDate()).thenReturn(1704067200L);
            when(subscription.getCurrentPeriodEnd()).thenReturn(1706745600L);
            when(subscription.getTrialEnd()).thenReturn(null);
            when(subscription.getCanceledAt()).thenReturn(1705363200L);
            when(subscription.getItems()).thenReturn(items);
            when(items.getData()).thenReturn(List.of(subscriptionItem));
            when(subscriptionItem.getPrice()).thenReturn(price);
            when(price.getUnitAmount()).thenReturn(999L);
            when(price.getCurrency()).thenReturn("usd");
            when(price.getRecurring()).thenReturn(null);

            when(subscriptionRepository.findByExternalSubscriptionId("sub_test_canceled")).thenReturn(Optional.empty());

            stripeService.handleWebhookEvent(event);

            var captor = ArgumentCaptor.forClass(UserSubscription.class);
            verify(subscriptionRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
            assertThat(captor.getValue().getCancelledAt()).isNotNull();
        }

        @Test
        @DisplayName("Should handle customer.subscription.deleted")
        void shouldHandleSubscriptionDeleted() {
            var event = mock(Event.class);
            var subscription = mock(Subscription.class);
            var deserializer = mock(EventDataObjectDeserializer.class);
            var existingSubscription = UserSubscription.builder()
                    .id(1L)
                    .externalSubscriptionId("sub_test_del")
                    .status(SubscriptionStatus.ACTIVE)
                    .build();

            when(event.getType()).thenReturn("customer.subscription.deleted");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);
            when(deserializer.getObject()).thenReturn(Optional.of(subscription));
            when(subscription.getId()).thenReturn("sub_test_del");

            when(subscriptionRepository.findByExternalSubscriptionId("sub_test_del"))
                    .thenReturn(Optional.of(existingSubscription));

            stripeService.handleWebhookEvent(event);

            var captor = ArgumentCaptor.forClass(UserSubscription.class);
            verify(subscriptionRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
            assertThat(captor.getValue().getCancelledAt()).isNotNull();
        }

        @Test
        @DisplayName("Should handle customer.subscription.deleted when subscription not found")
        void shouldHandleSubscriptionDeletedWhenNotFound() {
            var event = mock(Event.class);
            var subscription = mock(Subscription.class);
            var deserializer = mock(EventDataObjectDeserializer.class);

            when(event.getType()).thenReturn("customer.subscription.deleted");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);
            when(deserializer.getObject()).thenReturn(Optional.of(subscription));
            when(subscription.getId()).thenReturn("sub_nonexistent");

            when(subscriptionRepository.findByExternalSubscriptionId("sub_nonexistent"))
                    .thenReturn(Optional.empty());

            stripeService.handleWebhookEvent(event);

            verify(subscriptionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should handle invoice.payment_succeeded")
        void shouldHandleInvoicePaymentSucceeded() {
            var event = mock(Event.class);
            var invoice = mock(Invoice.class);
            var deserializer = mock(EventDataObjectDeserializer.class);
            var existingSubscription = UserSubscription.builder()
                    .id(1L)
                    .externalSubscriptionId("sub_test_invoice")
                    .status(SubscriptionStatus.PAST_DUE)
                    .build();

            when(event.getType()).thenReturn("invoice.payment_succeeded");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);
            when(deserializer.getObject()).thenReturn(Optional.of(invoice));
            when(invoice.getSubscription()).thenReturn("sub_test_invoice");

            when(subscriptionRepository.findByExternalSubscriptionId("sub_test_invoice"))
                    .thenReturn(Optional.of(existingSubscription));

            stripeService.handleWebhookEvent(event);

            var captor = ArgumentCaptor.forClass(UserSubscription.class);
            verify(subscriptionRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should not update status when payment succeeded and already active")
        void shouldNotUpdateWhenAlreadyActive() {
            var event = mock(Event.class);
            var invoice = mock(Invoice.class);
            var deserializer = mock(EventDataObjectDeserializer.class);
            var existingSubscription = UserSubscription.builder()
                    .id(1L)
                    .externalSubscriptionId("sub_active")
                    .status(SubscriptionStatus.ACTIVE)
                    .build();

            when(event.getType()).thenReturn("invoice.payment_succeeded");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);
            when(deserializer.getObject()).thenReturn(Optional.of(invoice));
            when(invoice.getSubscription()).thenReturn("sub_active");

            when(subscriptionRepository.findByExternalSubscriptionId("sub_active"))
                    .thenReturn(Optional.of(existingSubscription));

            stripeService.handleWebhookEvent(event);

            verify(subscriptionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should skip invoice.payment_succeeded when no subscription")
        void shouldSkipInvoicePaymentSucceededWhenNoSubscription() {
            var event = mock(Event.class);
            var invoice = mock(Invoice.class);
            var deserializer = mock(EventDataObjectDeserializer.class);

            when(event.getType()).thenReturn("invoice.payment_succeeded");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);
            when(deserializer.getObject()).thenReturn(Optional.of(invoice));
            when(invoice.getSubscription()).thenReturn(null);

            stripeService.handleWebhookEvent(event);

            verify(subscriptionRepository, never()).findByExternalSubscriptionId(any());
        }

        @Test
        @DisplayName("Should handle invoice.payment_failed")
        void shouldHandleInvoicePaymentFailed() {
            var event = mock(Event.class);
            var invoice = mock(Invoice.class);
            var deserializer = mock(EventDataObjectDeserializer.class);
            var existingSubscription = UserSubscription.builder()
                    .id(1L)
                    .externalSubscriptionId("sub_test_failed")
                    .status(SubscriptionStatus.ACTIVE)
                    .build();

            when(event.getType()).thenReturn("invoice.payment_failed");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);
            when(deserializer.getObject()).thenReturn(Optional.of(invoice));
            when(invoice.getSubscription()).thenReturn("sub_test_failed");

            when(subscriptionRepository.findByExternalSubscriptionId("sub_test_failed"))
                    .thenReturn(Optional.of(existingSubscription));

            stripeService.handleWebhookEvent(event);

            var captor = ArgumentCaptor.forClass(UserSubscription.class);
            verify(subscriptionRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(SubscriptionStatus.PAST_DUE);
        }

        @Test
        @DisplayName("Should skip invoice.payment_failed when no subscription")
        void shouldSkipInvoicePaymentFailedWhenNoSubscription() {
            var event = mock(Event.class);
            var invoice = mock(Invoice.class);
            var deserializer = mock(EventDataObjectDeserializer.class);

            when(event.getType()).thenReturn("invoice.payment_failed");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);
            when(deserializer.getObject()).thenReturn(Optional.of(invoice));
            when(invoice.getSubscription()).thenReturn(null);

            stripeService.handleWebhookEvent(event);

            verify(subscriptionRepository, never()).findByExternalSubscriptionId(any());
        }

        @Test
        @DisplayName("Should handle unhandled event type")
        void shouldHandleUnhandledEventType() {
            var event = mock(Event.class);
            when(event.getType()).thenReturn("some.unknown.event");

            stripeService.handleWebhookEvent(event);

            verify(purchaseRepository, never()).save(any());
            verify(subscriptionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should map subscription status correctly")
        void shouldMapSubscriptionStatusCorrectly() {
            var event = mock(Event.class);
            var subscription = mock(Subscription.class);
            var deserializer = mock(EventDataObjectDeserializer.class);
            var items = mock(SubscriptionItemCollection.class);
            var subscriptionItem = mock(SubscriptionItem.class);
            var price = mock(Price.class);

            when(event.getType()).thenReturn("customer.subscription.created");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);
            when(deserializer.getObject()).thenReturn(Optional.of(subscription));
            when(subscription.getMetadata()).thenReturn(Map.of("user_id", "1"));
            when(subscription.getId()).thenReturn("sub_unpaid");
            when(subscription.getStatus()).thenReturn("unpaid");
            when(subscription.getStartDate()).thenReturn(1704067200L);
            when(subscription.getCurrentPeriodEnd()).thenReturn(1706745600L);
            when(subscription.getTrialEnd()).thenReturn(null);
            when(subscription.getCanceledAt()).thenReturn(null);
            when(subscription.getItems()).thenReturn(items);
            when(items.getData()).thenReturn(List.of(subscriptionItem));
            when(subscriptionItem.getPrice()).thenReturn(price);
            when(price.getUnitAmount()).thenReturn(999L);
            when(price.getCurrency()).thenReturn("usd");
            when(price.getRecurring()).thenReturn(null);

            when(subscriptionRepository.findByExternalSubscriptionId("sub_unpaid")).thenReturn(Optional.empty());

            stripeService.handleWebhookEvent(event);

            var captor = ArgumentCaptor.forClass(UserSubscription.class);
            verify(subscriptionRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
        }

        @Test
        @DisplayName("Should map unknown subscription status to expired")
        void shouldMapUnknownStatusToExpired() {
            var event = mock(Event.class);
            var subscription = mock(Subscription.class);
            var deserializer = mock(EventDataObjectDeserializer.class);
            var items = mock(SubscriptionItemCollection.class);
            var subscriptionItem = mock(SubscriptionItem.class);
            var price = mock(Price.class);

            when(event.getType()).thenReturn("customer.subscription.created");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);
            when(deserializer.getObject()).thenReturn(Optional.of(subscription));
            when(subscription.getMetadata()).thenReturn(Map.of("user_id", "1"));
            when(subscription.getId()).thenReturn("sub_unknown");
            when(subscription.getStatus()).thenReturn("some_unknown_status");
            when(subscription.getStartDate()).thenReturn(1704067200L);
            when(subscription.getCurrentPeriodEnd()).thenReturn(1706745600L);
            when(subscription.getTrialEnd()).thenReturn(null);
            when(subscription.getCanceledAt()).thenReturn(null);
            when(subscription.getItems()).thenReturn(items);
            when(items.getData()).thenReturn(List.of(subscriptionItem));
            when(subscriptionItem.getPrice()).thenReturn(price);
            when(price.getUnitAmount()).thenReturn(999L);
            when(price.getCurrency()).thenReturn("usd");
            when(price.getRecurring()).thenReturn(null);

            when(subscriptionRepository.findByExternalSubscriptionId("sub_unknown")).thenReturn(Optional.empty());

            stripeService.handleWebhookEvent(event);

            var captor = ArgumentCaptor.forClass(UserSubscription.class);
            verify(subscriptionRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
        }

        @Test
        @DisplayName("Should handle subscription with empty items list")
        void shouldHandleSubscriptionWithEmptyItems() {
            var event = mock(Event.class);
            var subscription = mock(Subscription.class);
            var deserializer = mock(EventDataObjectDeserializer.class);
            var items = mock(SubscriptionItemCollection.class);

            when(event.getType()).thenReturn("customer.subscription.created");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);
            when(deserializer.getObject()).thenReturn(Optional.of(subscription));
            when(subscription.getMetadata()).thenReturn(Map.of("user_id", "1"));
            when(subscription.getId()).thenReturn("sub_empty_items");
            when(subscription.getStatus()).thenReturn("active");
            when(subscription.getStartDate()).thenReturn(1704067200L);
            when(subscription.getCurrentPeriodEnd()).thenReturn(1706745600L);
            when(subscription.getTrialEnd()).thenReturn(null);
            when(subscription.getCanceledAt()).thenReturn(null);
            when(subscription.getItems()).thenReturn(items);
            when(items.getData()).thenReturn(List.of());

            when(subscriptionRepository.findByExternalSubscriptionId("sub_empty_items")).thenReturn(Optional.empty());

            stripeService.handleWebhookEvent(event);

            var captor = ArgumentCaptor.forClass(UserSubscription.class);
            verify(subscriptionRepository).save(captor.capture());
            assertThat(captor.getValue().getPriceAmount()).isNull();
        }
    }
}
