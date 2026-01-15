package com.relyon.metasmart.service;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.relyon.metasmart.service.impl.LoggingEmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoggingEmailServiceTest {

    private LoggingEmailService loggingEmailService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_USER_NAME = "Test User";
    private static final String TEST_RESET_TOKEN = "reset-token-123";
    private static final String TEST_GOAL_TITLE = "Learn Java";

    @BeforeEach
    void setUp() {
        loggingEmailService = new LoggingEmailService();
    }

    @Nested
    @DisplayName("sendPasswordResetEmail")
    class SendPasswordResetEmail {

        @Test
        @DisplayName("should log password reset email without throwing exception")
        void shouldLogPasswordResetEmail() {
            assertThatCode(() -> loggingEmailService.sendPasswordResetEmail(
                    TEST_EMAIL, TEST_RESET_TOKEN, TEST_USER_NAME
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should handle null userName gracefully")
        void shouldHandleNullUserName() {
            assertThatCode(() -> loggingEmailService.sendPasswordResetEmail(
                    TEST_EMAIL, TEST_RESET_TOKEN, null
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should handle empty resetToken")
        void shouldHandleEmptyResetToken() {
            assertThatCode(() -> loggingEmailService.sendPasswordResetEmail(
                    TEST_EMAIL, "", TEST_USER_NAME
            )).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("sendWelcomeEmail")
    class SendWelcomeEmail {

        @Test
        @DisplayName("should log welcome email without throwing exception")
        void shouldLogWelcomeEmail() {
            assertThatCode(() -> loggingEmailService.sendWelcomeEmail(
                    TEST_EMAIL, TEST_USER_NAME
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should handle null userName gracefully")
        void shouldHandleNullUserName() {
            assertThatCode(() -> loggingEmailService.sendWelcomeEmail(
                    TEST_EMAIL, null
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should handle empty email")
        void shouldHandleEmptyEmail() {
            assertThatCode(() -> loggingEmailService.sendWelcomeEmail(
                    "", TEST_USER_NAME
            )).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("sendStreakAtRiskEmail")
    class SendStreakAtRiskEmail {

        @Test
        @DisplayName("should log streak at risk email without throwing exception")
        void shouldLogStreakAtRiskEmail() {
            assertThatCode(() -> loggingEmailService.sendStreakAtRiskEmail(
                    TEST_EMAIL, TEST_USER_NAME, TEST_GOAL_TITLE, 30
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should handle zero streak count")
        void shouldHandleZeroStreakCount() {
            assertThatCode(() -> loggingEmailService.sendStreakAtRiskEmail(
                    TEST_EMAIL, TEST_USER_NAME, TEST_GOAL_TITLE, 0
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should handle large streak count")
        void shouldHandleLargeStreakCount() {
            assertThatCode(() -> loggingEmailService.sendStreakAtRiskEmail(
                    TEST_EMAIL, TEST_USER_NAME, TEST_GOAL_TITLE, 365
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should handle null goal title")
        void shouldHandleNullGoalTitle() {
            assertThatCode(() -> loggingEmailService.sendStreakAtRiskEmail(
                    TEST_EMAIL, TEST_USER_NAME, null, 30
            )).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("sendMilestoneEmail")
    class SendMilestoneEmail {

        @Test
        @DisplayName("should log milestone email without throwing exception")
        void shouldLogMilestoneEmail() {
            assertThatCode(() -> loggingEmailService.sendMilestoneEmail(
                    TEST_EMAIL, TEST_USER_NAME, TEST_GOAL_TITLE, 50
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should handle 25% milestone")
        void shouldHandle25PercentMilestone() {
            assertThatCode(() -> loggingEmailService.sendMilestoneEmail(
                    TEST_EMAIL, TEST_USER_NAME, TEST_GOAL_TITLE, 25
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should handle 100% milestone")
        void shouldHandle100PercentMilestone() {
            assertThatCode(() -> loggingEmailService.sendMilestoneEmail(
                    TEST_EMAIL, TEST_USER_NAME, TEST_GOAL_TITLE, 100
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should handle zero percentage")
        void shouldHandleZeroPercentage() {
            assertThatCode(() -> loggingEmailService.sendMilestoneEmail(
                    TEST_EMAIL, TEST_USER_NAME, TEST_GOAL_TITLE, 0
            )).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("sendWeeklyDigestEmail")
    class SendWeeklyDigestEmail {

        @Test
        @DisplayName("should log weekly digest email without throwing exception")
        void shouldLogWeeklyDigestEmail() {
            assertThatCode(() -> loggingEmailService.sendWeeklyDigestEmail(
                    TEST_EMAIL, TEST_USER_NAME, 5, 3
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should handle zero goals count")
        void shouldHandleZeroGoalsCount() {
            assertThatCode(() -> loggingEmailService.sendWeeklyDigestEmail(
                    TEST_EMAIL, TEST_USER_NAME, 0, 0
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should handle large goals count")
        void shouldHandleLargeGoalsCount() {
            assertThatCode(() -> loggingEmailService.sendWeeklyDigestEmail(
                    TEST_EMAIL, TEST_USER_NAME, 100, 50
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should handle more milestones than goals")
        void shouldHandleMoreMilestonesThanGoals() {
            assertThatCode(() -> loggingEmailService.sendWeeklyDigestEmail(
                    TEST_EMAIL, TEST_USER_NAME, 3, 10
            )).doesNotThrowAnyException();
        }
    }
}
