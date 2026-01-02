package com.relyon.metasmart.exception;

public class UsageLimitExceededException extends RuntimeException {

    private final String limitType;
    private final int currentUsage;
    private final int maxAllowed;

    public UsageLimitExceededException(String limitType, int currentUsage, int maxAllowed) {
        super(String.format("Usage limit exceeded for %s: %d/%d. Upgrade to Premium for higher limits.",
                limitType, currentUsage, maxAllowed));
        this.limitType = limitType;
        this.currentUsage = currentUsage;
        this.maxAllowed = maxAllowed;
    }

    public String getLimitType() {
        return limitType;
    }

    public int getCurrentUsage() {
        return currentUsage;
    }

    public int getMaxAllowed() {
        return maxAllowed;
    }
}
