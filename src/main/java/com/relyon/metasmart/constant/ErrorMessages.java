package com.relyon.metasmart.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorMessages {

    public static final String USER_NOT_FOUND = "User not found";
    public static final String EMAIL_ALREADY_EXISTS = "Email already registered";
    public static final String INVALID_CREDENTIALS = "Invalid email or password";
    public static final String TOKEN_EXPIRED = "Token has expired";
    public static final String TOKEN_INVALID = "Invalid token";
    public static final String ACCESS_DENIED = "Access denied";
    public static final String VALIDATION_FAILED = "Validation failed";
    public static final String GOAL_NOT_FOUND = "Goal not found";
    public static final String GOAL_ACCESS_DENIED = "You don't have access to this goal";
    public static final String INVALID_DATE_RANGE = "Target date must be after start date";
    public static final String PROGRESS_ENTRY_NOT_FOUND = "Progress entry not found";
    public static final String MILESTONE_NOT_FOUND = "Milestone not found";
    public static final String MILESTONE_ALREADY_EXISTS = "Milestone with this percentage already exists";
}
