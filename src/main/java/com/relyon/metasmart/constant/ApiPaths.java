package com.relyon.metasmart.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApiPaths {

    public static final String API_V1 = "/api/v1";
    public static final String AUTH = API_V1 + "/auth";
    public static final String REGISTER = "/register";
    public static final String LOGIN = "/login";
    public static final String GOALS = API_V1 + "/goals";
    public static final String PROGRESS = "/progress";
    public static final String MILESTONES = "/milestones";
    public static final String ACTION_ITEMS = "/action-items";
    public static final String OBSTACLES = "/obstacles";
    public static final String GOAL_TEMPLATES = API_V1 + "/goal-templates";
    public static final String GUARDIANS = "/guardians";
    public static final String GUARDIAN = API_V1 + "/guardian";
    public static final String NUDGES = "/nudges";
}
