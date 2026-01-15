package com.relyon.metasmart.controller;

import com.relyon.metasmart.constant.ApiPaths;
import com.relyon.metasmart.entity.subscription.dto.PurchaseResponse;
import com.relyon.metasmart.entity.subscription.dto.SubscriptionResponse;
import com.relyon.metasmart.entity.subscription.dto.UserEntitlementsResponse;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(ApiPaths.API_V1 + "/subscription")
@RequiredArgsConstructor
@Tag(name = "Subscription")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    @Operation(summary = "Get current subscription")
    public ResponseEntity<SubscriptionResponse> getCurrentSubscription(@AuthenticationPrincipal User user) {
        log.debug("Getting subscription for user: {}", user.getEmail());
        return ResponseEntity.ok(subscriptionService.getCurrentSubscription(user));
    }

    @GetMapping("/entitlements")
    @Operation(summary = "Get user entitlements and feature access")
    public ResponseEntity<UserEntitlementsResponse> getEntitlements(@AuthenticationPrincipal User user) {
        log.debug("Getting entitlements for user: {}", user.getEmail());
        return ResponseEntity.ok(subscriptionService.getEntitlements(user));
    }

    @GetMapping("/purchases")
    @Operation(summary = "Get purchase history")
    public ResponseEntity<List<PurchaseResponse>> getPurchases(@AuthenticationPrincipal User user) {
        log.debug("Getting purchases for user: {}", user.getEmail());
        return ResponseEntity.ok(subscriptionService.getPurchases(user));
    }
}
