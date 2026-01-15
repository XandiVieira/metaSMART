package com.relyon.metasmart.service.impl;

import com.relyon.metasmart.exception.EmailSendingException;
import com.relyon.metasmart.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "metasmart.mail.enabled", havingValue = "true")
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${metasmart.mail.from}")
    private String fromEmail;

    @Value("${metasmart.mail.frontend-url}")
    private String frontendUrl;

    @Override
    @Async
    public void sendPasswordResetEmail(String to, String resetToken, String userName) {
        log.debug("Sending password reset email to: {}", to);

        var resetLink = frontendUrl + "/reset-password?token=" + resetToken;
        var subject = "Reset Your Metasmart Password";
        var content = buildPasswordResetHtml(userName, resetLink);

        sendHtmlEmail(to, subject, content);
        log.info("Password reset email sent to: {}", to);
    }

    @Override
    @Async
    public void sendWelcomeEmail(String to, String userName) {
        log.debug("Sending welcome email to: {}", to);

        var subject = "Welcome to Metasmart!";
        var content = buildWelcomeHtml(userName);

        sendHtmlEmail(to, subject, content);
        log.info("Welcome email sent to: {}", to);
    }

    @Override
    @Async
    public void sendStreakAtRiskEmail(String to, String userName, String goalTitle, int currentStreak) {
        log.debug("Sending streak at risk email to: {}", to);

        var subject = "Your " + currentStreak + "-Day Streak is at Risk!";
        var content = buildStreakAtRiskHtml(userName, goalTitle, currentStreak);

        sendHtmlEmail(to, subject, content);
        log.info("Streak at risk email sent to: {}", to);
    }

    @Override
    @Async
    public void sendMilestoneEmail(String to, String userName, String goalTitle, int percentage) {
        log.debug("Sending milestone email to: {}", to);

        var subject = "Congratulations! You've Reached " + percentage + "% on Your Goal!";
        var content = buildMilestoneHtml(userName, goalTitle, percentage);

        sendHtmlEmail(to, subject, content);
        log.info("Milestone email sent to: {}", to);
    }

    @Override
    @Async
    public void sendWeeklyDigestEmail(String to, String userName, int goalsCount, int completedMilestones) {
        log.debug("Sending weekly digest email to: {}", to);

        var subject = "Your Weekly Metasmart Summary";
        var content = buildWeeklyDigestHtml(userName, goalsCount, completedMilestones);

        sendHtmlEmail(to, subject, content);
        log.info("Weekly digest email sent to: {}", to);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            log.error("Failed to send email to: {}", to, e);
            throw new EmailSendingException("Failed to send email", e);
        }
    }

    private String buildPasswordResetHtml(String userName, String resetLink) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #4F46E5; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                        .content { background: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                        .button { display: inline-block; background: #4F46E5; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; margin: 20px 0; }
                        .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Metasmart</h1>
                        </div>
                        <div class="content">
                            <h2>Password Reset Request</h2>
                            <p>Hi %s,</p>
                            <p>We received a request to reset your password. Click the button below to create a new password:</p>
                            <p style="text-align: center;">
                                <a href="%s" class="button">Reset Password</a>
                            </p>
                            <p>This link will expire in 1 hour.</p>
                            <p>If you didn't request this, you can safely ignore this email.</p>
                        </div>
                        <div class="footer">
                            <p>&copy; Metasmart - Goal Management Platform</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(userName, resetLink);
    }

    private String buildWelcomeHtml(String userName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #4F46E5; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                        .content { background: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                        .button { display: inline-block; background: #4F46E5; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; margin: 20px 0; }
                        .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Welcome to Metasmart!</h1>
                        </div>
                        <div class="content">
                            <h2>Let's achieve your goals together!</h2>
                            <p>Hi %s,</p>
                            <p>Welcome to Metasmart! We're excited to help you define SMART goals and track your progress.</p>
                            <p>Here's what you can do:</p>
                            <ul>
                                <li>Create SMART goals with clear targets</li>
                                <li>Track your progress daily</li>
                                <li>Invite guardians for accountability</li>
                                <li>Reflect on your journey</li>
                            </ul>
                            <p>Start by creating your first goal!</p>
                        </div>
                        <div class="footer">
                            <p>&copy; Metasmart - Goal Management Platform</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(userName);
    }

    private String buildStreakAtRiskHtml(String userName, String goalTitle, int currentStreak) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #F59E0B; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                        .content { background: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                        .streak { font-size: 48px; text-align: center; color: #F59E0B; font-weight: bold; }
                        .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Don't Break Your Streak!</h1>
                        </div>
                        <div class="content">
                            <p>Hi %s,</p>
                            <p class="streak">%d days</p>
                            <p>Your streak for "<strong>%s</strong>" is at risk!</p>
                            <p>Log your progress today to keep your momentum going.</p>
                        </div>
                        <div class="footer">
                            <p>&copy; Metasmart - Goal Management Platform</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(userName, currentStreak, goalTitle);
    }

    private String buildMilestoneHtml(String userName, String goalTitle, int percentage) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #10B981; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                        .content { background: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                        .milestone { font-size: 48px; text-align: center; color: #10B981; font-weight: bold; }
                        .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Milestone Reached!</h1>
                        </div>
                        <div class="content">
                            <p>Hi %s,</p>
                            <p class="milestone">%d%%</p>
                            <p>Congratulations! You've reached <strong>%d%%</strong> on your goal "<strong>%s</strong>"!</p>
                            <p>Keep up the amazing work!</p>
                        </div>
                        <div class="footer">
                            <p>&copy; Metasmart - Goal Management Platform</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(userName, percentage, percentage, goalTitle);
    }

    private String buildWeeklyDigestHtml(String userName, int goalsCount, int completedMilestones) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #4F46E5; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                        .content { background: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                        .stats { display: flex; justify-content: space-around; margin: 20px 0; }
                        .stat { text-align: center; }
                        .stat-number { font-size: 36px; color: #4F46E5; font-weight: bold; }
                        .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Your Weekly Summary</h1>
                        </div>
                        <div class="content">
                            <p>Hi %s,</p>
                            <p>Here's your progress this week:</p>
                            <table style="width: 100%%; margin: 20px 0;">
                                <tr>
                                    <td style="text-align: center; padding: 20px;">
                                        <div style="font-size: 36px; color: #4F46E5; font-weight: bold;">%d</div>
                                        <div>Active Goals</div>
                                    </td>
                                    <td style="text-align: center; padding: 20px;">
                                        <div style="font-size: 36px; color: #10B981; font-weight: bold;">%d</div>
                                        <div>Milestones Hit</div>
                                    </td>
                                </tr>
                            </table>
                            <p>Keep pushing forward!</p>
                        </div>
                        <div class="footer">
                            <p>&copy; Metasmart - Goal Management Platform</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(userName, goalsCount, completedMilestones);
    }
}
