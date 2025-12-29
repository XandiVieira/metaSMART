package com.relyon.metasmart.entity.reflection;

public enum ReflectionFrequency {
    DAILY(1),
    EVERY_3_DAYS(3),
    WEEKLY(7),
    BI_WEEKLY(14);

    private final int days;

    ReflectionFrequency(int days) {
        this.days = days;
    }

    public int getDays() {
        return days;
    }

    public static ReflectionFrequency fromGoalDuration(long durationDays) {
        if (durationDays <= 14) {
            return DAILY;
        } else if (durationDays <= 60) {
            return EVERY_3_DAYS;
        } else if (durationDays <= 180) {
            return WEEKLY;
        } else {
            return BI_WEEKLY;
        }
    }
}
