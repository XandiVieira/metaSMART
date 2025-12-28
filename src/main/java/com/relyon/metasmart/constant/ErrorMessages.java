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
    public static final String ACTION_ITEM_NOT_FOUND = "Action item not found";
    public static final String OBSTACLE_ENTRY_NOT_FOUND = "Obstacle entry not found";
    public static final String GOAL_TEMPLATE_NOT_FOUND = "Goal template not found";

    // Guardian related
    public static final String GUARDIAN_NOT_FOUND = "Guardian not found";
    public static final String GUARDIAN_INVITATION_NOT_FOUND = "Guardian invitation not found";
    public static final String GUARDIAN_ALREADY_EXISTS = "This user is already a guardian for this goal";
    public static final String CANNOT_BE_OWN_GUARDIAN = "You cannot be a guardian for your own goal";
    public static final String GUARDIAN_NOT_ACTIVE = "Guardian relationship is not active";
    public static final String GUARDIAN_PERMISSION_DENIED = "You don't have permission to perform this action";
    public static final String NUDGE_NOT_FOUND = "Nudge not found";
    public static final String NUDGE_ACCESS_DENIED = "You don't have access to this nudge";
}
