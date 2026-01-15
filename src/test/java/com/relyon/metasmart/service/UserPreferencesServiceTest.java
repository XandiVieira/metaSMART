package com.relyon.metasmart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.entity.user.UserPreferences;
import com.relyon.metasmart.entity.user.dto.UserPreferencesRequest;
import com.relyon.metasmart.repository.UserPreferencesRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserPreferencesServiceTest {

    @Mock
    private UserPreferencesRepository userPreferencesRepository;

    @InjectMocks
    private UserPreferencesService userPreferencesService;

    private User user;
    private UserPreferences preferences;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        preferences = UserPreferences.builder()
                .id(1L)
                .user(user)
                .timezone("UTC")
                .language("en")
                .emailNotifications(true)
                .pushNotifications(true)
                .weeklyDigest(true)
                .streakReminders(true)
                .guardianNudges(true)
                .preferredReminderTime("09:00")
                .build();
    }

    @Nested
    @DisplayName("Get preferences tests")
    class GetPreferencesTests {

        @Test
        @DisplayName("Should return existing preferences")
        void shouldReturnExistingPreferences() {
            when(userPreferencesRepository.findByUser(user)).thenReturn(Optional.of(preferences));

            var response = userPreferencesService.getPreferences(user);

            assertThat(response).isNotNull();
            assertThat(response.getTimezone()).isEqualTo("UTC");
            assertThat(response.getLanguage()).isEqualTo("en");
            verify(userPreferencesRepository).findByUser(user);
        }

        @Test
        @DisplayName("Should create default preferences when none exist")
        void shouldCreateDefaultPreferencesWhenNoneExist() {
            when(userPreferencesRepository.findByUser(user)).thenReturn(Optional.empty());
            when(userPreferencesRepository.save(any(UserPreferences.class))).thenReturn(preferences);

            var response = userPreferencesService.getPreferences(user);

            assertThat(response).isNotNull();
            verify(userPreferencesRepository).save(any(UserPreferences.class));
        }
    }

    @Nested
    @DisplayName("Update preferences tests")
    class UpdatePreferencesTests {

        @Test
        @DisplayName("Should update existing preferences")
        void shouldUpdateExistingPreferences() {
            var request = UserPreferencesRequest.builder()
                    .timezone("America/New_York")
                    .language("pt")
                    .emailNotifications(false)
                    .build();

            when(userPreferencesRepository.findByUser(user)).thenReturn(Optional.of(preferences));
            when(userPreferencesRepository.save(any(UserPreferences.class))).thenReturn(preferences);

            var response = userPreferencesService.updatePreferences(user, request);

            assertThat(response).isNotNull();
            verify(userPreferencesRepository).save(any(UserPreferences.class));
        }

        @Test
        @DisplayName("Should create and update preferences when none exist")
        void shouldCreateAndUpdatePreferencesWhenNoneExist() {
            var request = UserPreferencesRequest.builder()
                    .timezone("Europe/London")
                    .build();

            when(userPreferencesRepository.findByUser(user)).thenReturn(Optional.empty());
            when(userPreferencesRepository.save(any(UserPreferences.class))).thenReturn(preferences);

            var response = userPreferencesService.updatePreferences(user, request);

            assertThat(response).isNotNull();
            verify(userPreferencesRepository, times(2)).save(any(UserPreferences.class));
        }
    }
}
