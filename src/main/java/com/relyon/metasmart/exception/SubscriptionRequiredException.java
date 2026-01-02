package com.relyon.metasmart.exception;

public class SubscriptionRequiredException extends RuntimeException {

    public SubscriptionRequiredException(String message) {
        super(message);
    }

    public SubscriptionRequiredException(String feature, String requiredTier) {
        super(String.format("Feature '%s' requires %s subscription", feature, requiredTier));
    }
}
