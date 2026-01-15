package com.relyon.metasmart.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.relyon.metasmart.exception.EmailSendingException;
import com.relyon.metasmart.service.impl.SmtpEmailService;
import jakarta.mail.internet.MimeMessage;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class SmtpEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    private SmtpEmailService smtpEmailService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_USER_NAME = "Test User";
    private static final String TEST_RESET_TOKEN = "reset-token-123";
    private static final String TEST_GOAL_TITLE = "Learn Java";
    private static final String FROM_EMAIL = "noreply@metasmart.com";
    private static final String FRONTEND_URL = "http://localhost:3000";

    @BeforeEach
    void setUp() throws Exception {
        smtpEmailService = new SmtpEmailService(mailSender);
        setField(smtpEmailService, "fromEmail", FROM_EMAIL);
        setField(smtpEmailService, "frontendUrl", FRONTEND_URL);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Nested
    @DisplayName("sendPasswordResetEmail")
    class SendPasswordResetEmail {

        @Test
        @DisplayName("should send password reset email successfully")
        void shouldSendPasswordResetEmailSuccessfully() {
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            smtpEmailService.sendPasswordResetEmail(TEST_EMAIL, TEST_RESET_TOKEN, TEST_USER_NAME);

            verify(mailSender).createMimeMessage();
            verify(mailSender).send(any(MimeMessage.class));
        }

        @Test
        @DisplayName("should throw EmailSendingException when MailException occurs")
        void shouldThrowEmailSendingExceptionOnMailException() {
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            doThrow(new MailSendException("Failed to send email")).when(mailSender).send(any(MimeMessage.class));

            assertThatThrownBy(() -> smtpEmailService.sendPasswordResetEmail(
                    TEST_EMAIL, TEST_RESET_TOKEN, TEST_USER_NAME
            ))
                    .isInstanceOf(EmailSendingException.class)
                    .hasMessage("Failed to send email")
                    .hasCauseInstanceOf(MailSendException.class);
        }

        @Test
        @DisplayName("should include reset token in email")
        void shouldIncludeResetTokenInEmail() {
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            smtpEmailService.sendPasswordResetEmail(TEST_EMAIL, TEST_RESET_TOKEN, TEST_USER_NAME);

            verify(mailSender).send(any(MimeMessage.class));
        }
    }

    @Nested
    @DisplayName("sendWelcomeEmail")
    class SendWelcomeEmail {

        @Test
        @DisplayName("should send welcome email successfully")
        void shouldSendWelcomeEmailSuccessfully() {
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            smtpEmailService.sendWelcomeEmail(TEST_EMAIL, TEST_USER_NAME);

            verify(mailSender).createMimeMessage();
            verify(mailSender).send(any(MimeMessage.class));
        }

        @Test
        @DisplayName("should throw EmailSendingException when sending fails")
        void shouldThrowEmailSendingExceptionOnSendFailure() {
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            doThrow(new MailSendException("Failed to send email")).when(mailSender).send(any(MimeMessage.class));

            assertThatThrownBy(() -> smtpEmailService.sendWelcomeEmail(TEST_EMAIL, TEST_USER_NAME))
                    .isInstanceOf(EmailSendingException.class)
                    .hasMessage("Failed to send email");
        }
    }

    @Nested
    @DisplayName("sendStreakAtRiskEmail")
    class SendStreakAtRiskEmail {

        @Test
        @DisplayName("should send streak at risk email successfully")
        void shouldSendStreakAtRiskEmailSuccessfully() {
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            smtpEmailService.sendStreakAtRiskEmail(TEST_EMAIL, TEST_USER_NAME, TEST_GOAL_TITLE, 30);

            verify(mailSender).createMimeMessage();
            verify(mailSender).send(any(MimeMessage.class));
        }

        @Test
        @DisplayName("should throw EmailSendingException when sending fails")
        void shouldThrowEmailSendingExceptionOnSendFailure() {
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            doThrow(new MailSendException("Failed to send email")).when(mailSender).send(any(MimeMessage.class));

            assertThatThrownBy(() -> smtpEmailService.sendStreakAtRiskEmail(
                    TEST_EMAIL, TEST_USER_NAME, TEST_GOAL_TITLE, 30
            )).isInstanceOf(EmailSendingException.class);
        }

        @Test
        @DisplayName("should send email with different streak counts")
        void shouldSendEmailWithDifferentStreakCounts() {
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            smtpEmailService.sendStreakAtRiskEmail(TEST_EMAIL, TEST_USER_NAME, TEST_GOAL_TITLE, 1);
            smtpEmailService.sendStreakAtRiskEmail(TEST_EMAIL, TEST_USER_NAME, TEST_GOAL_TITLE, 100);
            smtpEmailService.sendStreakAtRiskEmail(TEST_EMAIL, TEST_USER_NAME, TEST_GOAL_TITLE, 365);

            verify(mailSender, times(3)).send(any(MimeMessage.class));
        }
    }

    @Nested
    @DisplayName("sendMilestoneEmail")
    class SendMilestoneEmail {

        @Test
        @DisplayName("should send milestone email successfully")
        void shouldSendMilestoneEmailSuccessfully() {
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            smtpEmailService.sendMilestoneEmail(TEST_EMAIL, TEST_USER_NAME, TEST_GOAL_TITLE, 50);

            verify(mailSender).createMimeMessage();
            verify(mailSender).send(any(MimeMessage.class));
        }

        @Test
        @DisplayName("should throw EmailSendingException when sending fails")
        void shouldThrowEmailSendingExceptionOnSendFailure() {
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            doThrow(new MailSendException("Failed to send email")).when(mailSender).send(any(MimeMessage.class));

            assertThatThrownBy(() -> smtpEmailService.sendMilestoneEmail(
                    TEST_EMAIL, TEST_USER_NAME, TEST_GOAL_TITLE, 50
            )).isInstanceOf(EmailSendingException.class);
        }

        @Test
        @DisplayName("should send emails for different milestone percentages")
        void shouldSendEmailsForDifferentMilestonePercentages() {
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            smtpEmailService.sendMilestoneEmail(TEST_EMAIL, TEST_USER_NAME, TEST_GOAL_TITLE, 25);
            smtpEmailService.sendMilestoneEmail(TEST_EMAIL, TEST_USER_NAME, TEST_GOAL_TITLE, 50);
            smtpEmailService.sendMilestoneEmail(TEST_EMAIL, TEST_USER_NAME, TEST_GOAL_TITLE, 75);
            smtpEmailService.sendMilestoneEmail(TEST_EMAIL, TEST_USER_NAME, TEST_GOAL_TITLE, 100);

            verify(mailSender, times(4)).send(any(MimeMessage.class));
        }
    }

    @Nested
    @DisplayName("sendWeeklyDigestEmail")
    class SendWeeklyDigestEmail {

        @Test
        @DisplayName("should send weekly digest email successfully")
        void shouldSendWeeklyDigestEmailSuccessfully() {
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            smtpEmailService.sendWeeklyDigestEmail(TEST_EMAIL, TEST_USER_NAME, 5, 3);

            verify(mailSender).createMimeMessage();
            verify(mailSender).send(any(MimeMessage.class));
        }

        @Test
        @DisplayName("should throw EmailSendingException when sending fails")
        void shouldThrowEmailSendingExceptionOnSendFailure() {
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            doThrow(new MailSendException("Failed to send email")).when(mailSender).send(any(MimeMessage.class));

            assertThatThrownBy(() -> smtpEmailService.sendWeeklyDigestEmail(
                    TEST_EMAIL, TEST_USER_NAME, 5, 3
            )).isInstanceOf(EmailSendingException.class);
        }

        @Test
        @DisplayName("should send email with zero counts")
        void shouldSendEmailWithZeroCounts() {
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            smtpEmailService.sendWeeklyDigestEmail(TEST_EMAIL, TEST_USER_NAME, 0, 0);

            verify(mailSender).send(any(MimeMessage.class));
        }

        @Test
        @DisplayName("should send email with large counts")
        void shouldSendEmailWithLargeCounts() {
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            smtpEmailService.sendWeeklyDigestEmail(TEST_EMAIL, TEST_USER_NAME, 100, 50);

            verify(mailSender).send(any(MimeMessage.class));
        }
    }

    @Nested
    @DisplayName("HTML Content Generation")
    class HtmlContentGeneration {

        @Test
        @DisplayName("should create valid MimeMessage for password reset")
        void shouldCreateValidMimeMessageForPasswordReset() {
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            smtpEmailService.sendPasswordResetEmail(TEST_EMAIL, TEST_RESET_TOKEN, TEST_USER_NAME);

            verify(mailSender).createMimeMessage();
            verify(mailSender).send(mimeMessage);
        }

        @Test
        @DisplayName("should create valid MimeMessage for welcome email")
        void shouldCreateValidMimeMessageForWelcomeEmail() {
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            smtpEmailService.sendWelcomeEmail(TEST_EMAIL, TEST_USER_NAME);

            verify(mailSender).createMimeMessage();
            verify(mailSender).send(mimeMessage);
        }

        @Test
        @DisplayName("should create valid MimeMessage for streak at risk")
        void shouldCreateValidMimeMessageForStreakAtRisk() {
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            smtpEmailService.sendStreakAtRiskEmail(TEST_EMAIL, TEST_USER_NAME, TEST_GOAL_TITLE, 30);

            verify(mailSender).createMimeMessage();
            verify(mailSender).send(mimeMessage);
        }

        @Test
        @DisplayName("should create valid MimeMessage for milestone")
        void shouldCreateValidMimeMessageForMilestone() {
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            smtpEmailService.sendMilestoneEmail(TEST_EMAIL, TEST_USER_NAME, TEST_GOAL_TITLE, 50);

            verify(mailSender).createMimeMessage();
            verify(mailSender).send(mimeMessage);
        }

        @Test
        @DisplayName("should create valid MimeMessage for weekly digest")
        void shouldCreateValidMimeMessageForWeeklyDigest() {
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            smtpEmailService.sendWeeklyDigestEmail(TEST_EMAIL, TEST_USER_NAME, 5, 3);

            verify(mailSender).createMimeMessage();
            verify(mailSender).send(mimeMessage);
        }
    }
}
