package com.relyon.metasmart.entity.reflection;

public enum ReflectionRating {
    TERRIBLE(1),
    POOR(2),
    OKAY(3),
    GOOD(4),
    EXCELLENT(5);

    private final int value;

    ReflectionRating(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
