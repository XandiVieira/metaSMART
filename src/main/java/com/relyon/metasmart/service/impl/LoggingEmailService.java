package com.relyon.metasmart.service.impl;

import com.relyon.metasmart.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LoggingEmailService implements EmailService {

    private static final String EMAIL_BANNER = """

            ╔══════════════════════════════════════════════════════════════╗
            ║                     📧 EMAIL NOTIFICATION                     ║
            ╠══════════════════════════════════════════════════════════════╣
            """;

    private static final String EMAIL_FOOTER = """
            ╚══════════════════════════════════════════════════════════════╝
            """;

    @Override
    public void sendPasswordResetEmail(String to, String resetToken, String userName) {
        log.info(EMAIL_BANNER +
                "║  Type: PASSWORD RESET                                        ║\n" +
                "║  To: {}\n" +
                "║  User: {}\n" +
                "║  Reset Token: {}\n" +
                "║  Reset Link: http://localhost:3000/reset-password?token={}\n" +
                EMAIL_FOOTER,
                to, userName, resetToken, resetToken);
    }

    @Override
    public void sendWelcomeEmail(String to, String userName) {
        log.info(EMAIL_BANNER +
                "║  Type: WELCOME                                               ║\n" +
                "║  To: {}\n" +
                "║  User: {}\n" +
                "║  Message: Welcome to Metasmart! Start setting your goals.   ║\n" +
                EMAIL_FOOTER,
                to, userName);
    }

    @Override
    public void sendStreakAtRiskEmail(String to, String userName, String goalTitle, int currentStreak) {
        log.info(EMAIL_BANNER +
                "║  Type: STREAK AT RISK                                        ║\n" +
                "║  To: {}\n" +
                "║  User: {}\n" +
                "║  Goal: {}\n" +
                "║  Current Streak: {} days\n" +
                "║  Message: Don't lose your streak! Log progress today.       ║\n" +
                EMAIL_FOOTER,
                to, userName, goalTitle, currentStreak);
    }

    @Override
    public void sendMilestoneEmail(String to, String userName, String goalTitle, int percentage) {
        log.info(EMAIL_BANNER +
                "║  Type: MILESTONE REACHED                                     ║\n" +
                "║  To: {}\n" +
                "║  User: {}\n" +
                "║  Goal: {}\n" +
                "║  Milestone: {}%\n" +
                "║  Message: Congratulations on reaching this milestone!       ║\n" +
                EMAIL_FOOTER,
                to, userName, goalTitle, percentage);
    }

    @Override
    public void sendWeeklyDigestEmail(String to, String userName, int goalsCount, int completedMilestones) {
        log.info(EMAIL_BANNER +
                "║  Type: WEEKLY DIGEST                                         ║\n" +
                "║  To: {}\n" +
                "║  User: {}\n" +
                "║  Active Goals: {}\n" +
                "║  Milestones Completed This Week: {}\n" +
                "║  Message: Here's your weekly progress summary!              ║\n" +
                EMAIL_FOOTER,
                to, userName, goalsCount, completedMilestones);
    }
}
