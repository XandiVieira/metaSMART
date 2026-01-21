package com.relyon.metasmart.constant;

import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AppConstants {

    // Milestone percentages
    public static final int MILESTONE_QUARTER = 25;
    public static final int MILESTONE_HALF = 50;
    public static final int MILESTONE_THREE_QUARTERS = 75;
    public static final int MILESTONE_COMPLETE = 100;
    public static final List<Integer> DEFAULT_MILESTONE_PERCENTAGES = List.of(
            MILESTONE_QUARTER, MILESTONE_HALF, MILESTONE_THREE_QUARTERS, MILESTONE_COMPLETE
    );

    // Streak shield awards
    public static final int STREAK_SHIELD_AWARD_MILESTONE_HALF = 50;
    public static final int STREAK_SHIELD_AWARD_MILESTONE_COMPLETE = 100;

    // SMART pillars count
    public static final int SMART_PILLARS_COUNT = 5;

    // Currency conversion (Stripe uses cents)
    public static final int CURRENCY_CENTS_DIVISOR = 100;

    // Pagination defaults
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    // Subscription Tier Limits - FREE
    public static final int FREE_MAX_GOALS = 2;
    public static final int FREE_MAX_GUARDIANS_PER_GOAL = 1;
    public static final int FREE_PROGRESS_HISTORY_DAYS = 30;
    public static final int FREE_STREAK_SHIELDS_PER_MONTH = 1;
    public static final int FREE_STRUGGLING_REQUESTS_PER_MONTH = 1;

    // Subscription Tier Limits - PREMIUM
    public static final int PREMIUM_MAX_GUARDIANS_PER_GOAL = 5;
    public static final int PREMIUM_PROGRESS_HISTORY_DAYS = Integer.MAX_VALUE; // Unlimited

    // Streak shields
    public static final int MAX_STREAK_SHIELDS = 2;
    public static final int CONSECUTIVE_JOURNAL_DAYS_FOR_SHIELD = 7;
    public static final int SHIELDS_PER_WEEK = 1;
}
