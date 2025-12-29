package com.relyon.metasmart.service;

import com.relyon.metasmart.entity.notification.NotificationPreferences;
import com.relyon.metasmart.entity.notification.dto.NotificationPreferencesRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.repository.NotificationPreferencesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationPreferencesServiceTest {

    @Mock
    private NotificationPreferencesRepository notificationPreferencesRepository;

    @InjectMocks
    private NotificationPreferencesService notificationPreferencesService;

    private User user;
    private NotificationPreferences preferences;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        preferences = NotificationPreferences.builder()
                .id(1L)
                .user(user)
                .pushEnabled(true)
                .pushGoalReminders(true)
                .pushProgressReminders(true)
                .pushMilestones(true)
                .pushStreakAlerts(true)
                .pushGuardianNudges(true)
                .emailEnabled(true)
                .emailWeeklyDigest(true)
                .emailMilestones(true)
                .emailStreakAtRisk(true)
                .whatsappEnabled(false)
                .quietHoursEnabled(false)
                .build();
    }

    @Nested
    @DisplayName("Get notification preferences tests")
    class GetNotificationPreferencesTests {

        @Test
        @DisplayName("Should return existing notification preferences")
        void shouldReturnExistingNotificationPreferences() {
            when(notificationPreferencesRepository.findByUser(user)).thenReturn(Optional.of(preferences));

            var response = notificationPreferencesService.getPreferences(user);

            assertThat(response).isNotNull();
            assertThat(response.getPushEnabled()).isTrue();
            assertThat(response.getEmailEnabled()).isTrue();
            verify(notificationPreferencesRepository).findByUser(user);
        }

        @Test
        @DisplayName("Should create default notification preferences when none exist")
        void shouldCreateDefaultNotificationPreferencesWhenNoneExist() {
            when(notificationPreferencesRepository.findByUser(user)).thenReturn(Optional.empty());
            when(notificationPreferencesRepository.save(any(NotificationPreferences.class))).thenReturn(preferences);

            var response = notificationPreferencesService.getPreferences(user);

            assertThat(response).isNotNull();
            verify(notificationPreferencesRepository).save(any(NotificationPreferences.class));
        }
    }

    @Nested
    @DisplayName("Update notification preferences tests")
    class UpdateNotificationPreferencesTests {

        @Test
        @DisplayName("Should update existing notification preferences")
        void shouldUpdateExistingNotificationPreferences() {
            var request = NotificationPreferencesRequest.builder()
                    .pushEnabled(false)
                    .emailEnabled(false)
                    .whatsappEnabled(true)
                    .whatsappNumber("+5511999999999")
                    .quietHoursEnabled(true)
                    .quietHoursStart("22:00")
                    .quietHoursEnd("08:00")
                    .build();

            when(notificationPreferencesRepository.findByUser(user)).thenReturn(Optional.of(preferences));
            when(notificationPreferencesRepository.save(any(NotificationPreferences.class))).thenReturn(preferences);

            var response = notificationPreferencesService.updatePreferences(user, request);

            assertThat(response).isNotNull();
            verify(notificationPreferencesRepository).save(any(NotificationPreferences.class));
        }

        @Test
        @DisplayName("Should create and update notification preferences when none exist")
        void shouldCreateAndUpdateNotificationPreferencesWhenNoneExist() {
            var request = NotificationPreferencesRequest.builder()
                    .pushEnabled(false)
                    .build();

            when(notificationPreferencesRepository.findByUser(user)).thenReturn(Optional.empty());
            when(notificationPreferencesRepository.save(any(NotificationPreferences.class))).thenReturn(preferences);

            var response = notificationPreferencesService.updatePreferences(user, request);

            assertThat(response).isNotNull();
            verify(notificationPreferencesRepository, times(2)).save(any(NotificationPreferences.class));
        }

        @Test
        @DisplayName("Should update push settings only")
        void shouldUpdatePushSettingsOnly() {
            var request = NotificationPreferencesRequest.builder()
                    .pushGoalReminders(false)
                    .pushProgressReminders(false)
                    .pushMilestones(false)
                    .pushStreakAlerts(false)
                    .pushGuardianNudges(false)
                    .build();

            when(notificationPreferencesRepository.findByUser(user)).thenReturn(Optional.of(preferences));
            when(notificationPreferencesRepository.save(any(NotificationPreferences.class))).thenReturn(preferences);

            var response = notificationPreferencesService.updatePreferences(user, request);

            assertThat(response).isNotNull();
            verify(notificationPreferencesRepository).save(any(NotificationPreferences.class));
        }

        @Test
        @DisplayName("Should update email settings only")
        void shouldUpdateEmailSettingsOnly() {
            var request = NotificationPreferencesRequest.builder()
                    .emailWeeklyDigest(false)
                    .emailMilestones(false)
                    .emailStreakAtRisk(false)
                    .build();

            when(notificationPreferencesRepository.findByUser(user)).thenReturn(Optional.of(preferences));
            when(notificationPreferencesRepository.save(any(NotificationPreferences.class))).thenReturn(preferences);

            var response = notificationPreferencesService.updatePreferences(user, request);

            assertThat(response).isNotNull();
            verify(notificationPreferencesRepository).save(any(NotificationPreferences.class));
        }
    }
}
