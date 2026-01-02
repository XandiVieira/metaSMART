package com.relyon.metasmart.controller;

import com.relyon.metasmart.entity.subscription.dto.CheckoutResponse;
import com.relyon.metasmart.entity.subscription.dto.CreateCheckoutRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.service.StripeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Stripe payment integration")
public class StripeController {

    private final StripeService stripeService;

    @PostMapping("/checkout")
    @Operation(summary = "Create a checkout session for subscription or one-time purchase")
    public ResponseEntity<CheckoutResponse> createCheckoutSession(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateCheckoutRequest request) {
        log.debug("Creating checkout session for user ID: {}", user.getId());
        var response = stripeService.createCheckoutSession(user, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    @Operation(summary = "Handle Stripe webhook events")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        log.debug("Received Stripe webhook");
        var event = stripeService.constructEvent(payload, sigHeader);
        stripeService.handleWebhookEvent(event);
        return ResponseEntity.ok("Received");
    }
}
