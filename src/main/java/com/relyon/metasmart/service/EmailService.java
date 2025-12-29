package com.relyon.metasmart.service;

public interface EmailService {

    void sendPasswordResetEmail(String to, String resetToken, String userName);

    void sendWelcomeEmail(String to, String userName);

    void sendStreakAtRiskEmail(String to, String userName, String goalTitle, int currentStreak);

    void sendMilestoneEmail(String to, String userName, String goalTitle, int percentage);

    void sendWeeklyDigestEmail(String to, String userName, int goalsCount, int completedMilestones);
}
