package com.relyon.metasmart.service;

import static com.relyon.metasmart.constant.ErrorMessages.*;

import com.relyon.metasmart.config.StripeConfig;
import com.relyon.metasmart.entity.subscription.*;
import com.relyon.metasmart.entity.subscription.dto.CheckoutResponse;
import com.relyon.metasmart.entity.subscription.dto.CreateCheckoutRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.BadRequestException;
import com.relyon.metasmart.exception.PaymentProcessingException;
import com.relyon.metasmart.repository.UserPurchaseRepository;
import com.relyon.metasmart.repository.UserSubscriptionRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeService {

    private static final String PAYMENT_PROVIDER = "stripe";
    private static final String METADATA_USER_ID = "user_id";
    private static final String METADATA_PRODUCT_TYPE = "product_type";
    private static final String METADATA_QUANTITY = "quantity";

    private final StripeConfig stripeConfig;
    private final UserSubscriptionRepository subscriptionRepository;
    private final UserPurchaseRepository purchaseRepository;

    @Value("${metasmart.mail.frontend-url}")
    private String frontendUrl;

    public CheckoutResponse createCheckoutSession(User user, CreateCheckoutRequest request) {
        log.debug("Creating checkout session for user ID: {} with product: {}", user.getId(), request.getProductType());

        validateStripeConfiguration();

        var priceId = getPriceId(request);
        var mode = getSessionMode(request);
        var quantity = request.getQuantity() != null ? request.getQuantity() : 1;

        try {
            var paramsBuilder = SessionCreateParams.builder()
                    .setMode(mode)
                    .setCustomerEmail(user.getEmail())
                    .setSuccessUrl(frontendUrl + "/payment/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(frontendUrl + "/payment/cancel")
                    .putMetadata(METADATA_USER_ID, user.getId().toString())
                    .putMetadata(METADATA_PRODUCT_TYPE, request.getProductType().name())
                    .putMetadata(METADATA_QUANTITY, String.valueOf(quantity))
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPrice(priceId)
                                    .setQuantity((long) quantity)
                                    .build()
                    );

            if (mode == SessionCreateParams.Mode.SUBSCRIPTION) {
                paramsBuilder.setSubscriptionData(
                        SessionCreateParams.SubscriptionData.builder()
                                .putMetadata(METADATA_USER_ID, user.getId().toString())
                                .build()
                );
            }

            var session = Session.create(paramsBuilder.build());
            log.info("Checkout session created: {} for user ID: {}", session.getId(), user.getId());

            return CheckoutResponse.builder()
                    .sessionId(session.getId())
                    .url(session.getUrl())
                    .build();

        } catch (StripeException e) {
            log.error("Failed to create checkout session for user ID: {}", user.getId(), e);
            throw new PaymentProcessingException(FAILED_TO_CREATE_PAYMENT_SESSION, e);
        }
    }

    public Event constructEvent(String payload, String sigHeader) {
        try {
            return Webhook.constructEvent(payload, sigHeader, stripeConfig.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            log.error("Invalid webhook signature", e);
            throw new BadRequestException("Invalid webhook signature");
        }
    }

    @Transactional
    public void handleWebhookEvent(Event event) {
        log.debug("Processing Stripe webhook event: {}", event.getType());

        switch (event.getType()) {
            case "checkout.session.completed" -> handleCheckoutCompleted(event);
            case "customer.subscription.created" -> handleSubscriptionCreated(event);
            case "customer.subscription.updated" -> handleSubscriptionUpdated(event);
            case "customer.subscription.deleted" -> handleSubscriptionDeleted(event);
            case "invoice.payment_succeeded" -> handleInvoicePaymentSucceeded(event);
            case "invoice.payment_failed" -> handleInvoicePaymentFailed(event);
            default -> log.debug("Unhandled event type: {}", event.getType());
        }
    }

    private void handleCheckoutCompleted(Event event) {
        var session = (Session) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new PaymentProcessingException(FAILED_TO_DESERIALIZE_SESSION));

        var metadata = session.getMetadata();
        var userId = Long.parseLong(metadata.get(METADATA_USER_ID));
        var productType = CreateCheckoutRequest.ProductType.valueOf(metadata.get(METADATA_PRODUCT_TYPE));
        var quantity = Integer.parseInt(metadata.getOrDefault(METADATA_QUANTITY, "1"));

        log.info("Checkout completed for user ID: {}, product: {}", userId, productType);

        if (productType != CreateCheckoutRequest.ProductType.PREMIUM_SUBSCRIPTION) {
            createPurchase(userId, productType, quantity, session);
        }
    }

    private void createPurchase(Long userId, CreateCheckoutRequest.ProductType productType, int quantity, Session session) {
        var purchaseType = switch (productType) {
            case STREAK_SHIELD -> PurchaseType.STREAK_SHIELD;
            case STRUGGLING_ASSIST -> PurchaseType.STRUGGLING_ASSIST;
            case GOAL_BOOST -> PurchaseType.GOAL_BOOST;
            case GUARDIAN_SLOT -> PurchaseType.GUARDIAN_SLOT;
            default -> throw new IllegalArgumentException("Invalid purchase type: " + productType);
        };

        var user = new User();
        user.setId(userId);

        var purchase = UserPurchase.builder()
                .user(user)
                .purchaseType(purchaseType)
                .quantity(quantity)
                .quantityRemaining(quantity)
                .priceAmount(session.getAmountTotal() != null
                        ? BigDecimal.valueOf(session.getAmountTotal()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                        : null)
                .priceCurrency(session.getCurrency())
                .externalTransactionId(session.getPaymentIntent() != null ? session.getPaymentIntent() : session.getId())
                .paymentProvider(PAYMENT_PROVIDER)
                .purchasedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusYears(1))
                .build();

        purchaseRepository.save(purchase);
        log.info("Purchase created for user ID: {}, type: {}, quantity: {}", userId, purchaseType, quantity);
    }

    private void handleSubscriptionCreated(Event event) {
        handleSubscriptionChange(event);
    }

    private void handleSubscriptionUpdated(Event event) {
        handleSubscriptionChange(event);
    }

    private void handleSubscriptionChange(Event event) {
        var subscription = (Subscription) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new PaymentProcessingException(FAILED_TO_DESERIALIZE_SUBSCRIPTION));

        var userId = extractUserIdFromSubscription(subscription);
        if (userId.isEmpty()) {
            log.warn("No user ID found in subscription metadata: {}", subscription.getId());
            return;
        }

        createOrUpdateSubscription(userId.get(), subscription);
    }

    private void handleSubscriptionDeleted(Event event) {
        var subscription = (Subscription) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new PaymentProcessingException(FAILED_TO_DESERIALIZE_SUBSCRIPTION));

        subscriptionRepository.findByExternalSubscriptionId(subscription.getId())
                .ifPresent(userSubscription -> {
                    userSubscription.setStatus(SubscriptionStatus.CANCELLED);
                    userSubscription.setCancelledAt(LocalDateTime.now());
                    subscriptionRepository.save(userSubscription);
                    log.info("Subscription cancelled: {}", subscription.getId());
                });
    }

    private void handleInvoicePaymentSucceeded(Event event) {
        var invoice = (Invoice) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new PaymentProcessingException(FAILED_TO_DESERIALIZE_INVOICE));

        if (invoice.getSubscription() == null) {
            return;
        }

        subscriptionRepository.findByExternalSubscriptionId(invoice.getSubscription())
                .ifPresent(userSubscription -> {
                    if (userSubscription.getStatus() == SubscriptionStatus.PAST_DUE) {
                        userSubscription.setStatus(SubscriptionStatus.ACTIVE);
                        subscriptionRepository.save(userSubscription);
                        log.info("Subscription reactivated after successful payment: {}", invoice.getSubscription());
                    }
                });
    }

    private void handleInvoicePaymentFailed(Event event) {
        var invoice = (Invoice) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new PaymentProcessingException(FAILED_TO_DESERIALIZE_INVOICE));

        if (invoice.getSubscription() == null) {
            return;
        }

        subscriptionRepository.findByExternalSubscriptionId(invoice.getSubscription())
                .ifPresent(userSubscription -> {
                    userSubscription.setStatus(SubscriptionStatus.PAST_DUE);
                    subscriptionRepository.save(userSubscription);
                    log.warn("Subscription marked as past due: {}", invoice.getSubscription());
                });
    }

    private void createOrUpdateSubscription(Long userId, Subscription stripeSubscription) {
        var user = new User();
        user.setId(userId);

        var existingSubscription = subscriptionRepository.findByExternalSubscriptionId(stripeSubscription.getId());

        var userSubscription = existingSubscription.orElseGet(() -> UserSubscription.builder()
                .user(user)
                .externalSubscriptionId(stripeSubscription.getId())
                .paymentProvider(PAYMENT_PROVIDER)
                .build());

        userSubscription.setTier(SubscriptionTier.PREMIUM);
        userSubscription.setStatus(mapSubscriptionStatus(stripeSubscription.getStatus()));
        userSubscription.setStartDate(toLocalDateTime(stripeSubscription.getStartDate()));
        userSubscription.setEndDate(toLocalDateTime(stripeSubscription.getCurrentPeriodEnd()));

        if (stripeSubscription.getTrialEnd() != null) {
            userSubscription.setTrialEndDate(toLocalDateTime(stripeSubscription.getTrialEnd()));
        }

        if (stripeSubscription.getCanceledAt() != null) {
            userSubscription.setCancelledAt(toLocalDateTime(stripeSubscription.getCanceledAt()));
        }

        var items = stripeSubscription.getItems().getData();
        if (!items.isEmpty()) {
            var price = items.getFirst().getPrice();
            userSubscription.setPriceAmount(BigDecimal.valueOf(price.getUnitAmount()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            userSubscription.setPriceCurrency(price.getCurrency());
            userSubscription.setBillingPeriod(price.getRecurring() != null ? price.getRecurring().getInterval() : null);
        }

        subscriptionRepository.save(userSubscription);
        log.info("Subscription saved for user ID: {}, status: {}", userId, userSubscription.getStatus());
    }

    private Optional<Long> extractUserIdFromSubscription(Subscription subscription) {
        var metadata = subscription.getMetadata();
        if (metadata != null && metadata.containsKey(METADATA_USER_ID)) {
            return Optional.of(Long.parseLong(metadata.get(METADATA_USER_ID)));
        }
        return Optional.empty();
    }

    private SubscriptionStatus mapSubscriptionStatus(String stripeStatus) {
        return switch (stripeStatus) {
            case "active" -> SubscriptionStatus.ACTIVE;
            case "trialing" -> SubscriptionStatus.TRIALING;
            case "past_due" -> SubscriptionStatus.PAST_DUE;
            case "canceled", "unpaid" -> SubscriptionStatus.CANCELLED;
            default -> SubscriptionStatus.EXPIRED;
        };
    }

    private LocalDateTime toLocalDateTime(Long epochSeconds) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.systemDefault());
    }

    private String getPriceId(CreateCheckoutRequest request) {
        var prices = stripeConfig.getPrices();

        return switch (request.getProductType()) {
            case PREMIUM_SUBSCRIPTION -> {
                if (request.getBillingPeriod() == CreateCheckoutRequest.BillingPeriod.YEARLY) {
                    yield prices.getPremiumYearly();
                }
                yield prices.getPremiumMonthly();
            }
            case STREAK_SHIELD -> prices.getStreakShield();
            case STRUGGLING_ASSIST -> prices.getStrugglingAssist();
            case GOAL_BOOST -> prices.getGoalBoost();
            case GUARDIAN_SLOT -> prices.getGuardianSlot();
        };
    }

    private SessionCreateParams.Mode getSessionMode(CreateCheckoutRequest request) {
        return request.getProductType() == CreateCheckoutRequest.ProductType.PREMIUM_SUBSCRIPTION
                ? SessionCreateParams.Mode.SUBSCRIPTION
                : SessionCreateParams.Mode.PAYMENT;
    }

    private void validateStripeConfiguration() {
        if (stripeConfig.getApiKey() == null || stripeConfig.getApiKey().isBlank()) {
            throw new BadRequestException("Stripe is not configured");
        }
    }
}
