package com.relyon.metasmart.config.subscription;

public enum Feature {

    UNLIMITED_GOALS("unlimitedGoals"),
    MULTIPLE_GUARDIANS("multipleGuardians"),
    UNLIMITED_HISTORY("unlimitedHistory"),
    CREATE_TEMPLATES("createTemplates"),
    AI_INSIGHTS("aiInsights"),
    DATA_EXPORT("dataExport"),
    PRIORITY_SUPPORT("prioritySupport");

    private final String key;

    Feature(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
