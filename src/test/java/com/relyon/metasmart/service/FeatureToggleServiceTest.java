package com.relyon.metasmart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.relyon.metasmart.entity.feature.FeaturePreferences;
import com.relyon.metasmart.entity.feature.dto.FeaturePreferencesRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.repository.FeaturePreferencesRepository;
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
class FeatureToggleServiceTest {

    @Mock
    private FeaturePreferencesRepository featurePreferencesRepository;

    @InjectMocks
    private FeatureToggleService featureToggleService;

    private User user;
    private FeaturePreferences preferences;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        preferences = FeaturePreferences.builder()
                .id(1L)
                .user(user)
                .dailyJournalEnabled(true)
                .streaksEnabled(true)
                .achievementsEnabled(true)
                .analyticsEnabled(true)
                .flightPlanEnabled(true)
                .progressRemindersEnabled(true)
                .milestonesEnabled(true)
                .obstacleTrackingEnabled(true)
                .reflectionsEnabled(true)
                .socialProofEnabled(true)
                .build();
    }

    @Nested
    @DisplayName("isFeatureEnabled tests")
    class IsFeatureEnabledTests {

        @Test
        @DisplayName("Should return true when no preferences exist (default behavior)")
        void shouldReturnTrueWhenNoPreferencesExist() {
            when(featurePreferencesRepository.findByUser(user)).thenReturn(Optional.empty());

            var result = featureToggleService.isFeatureEnabled(user, "dailyJournal");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return feature value when preferences exist")
        void shouldReturnFeatureValueWhenPreferencesExist() {
            preferences.setDailyJournalEnabled(false);
            when(featurePreferencesRepository.findByUser(user)).thenReturn(Optional.of(preferences));

            var result = featureToggleService.isFeatureEnabled(user, "dailyJournal");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return true for unknown feature name")
        void shouldReturnTrueForUnknownFeatureName() {
            when(featurePreferencesRepository.findByUser(user)).thenReturn(Optional.of(preferences));

            var result = featureToggleService.isFeatureEnabled(user, "unknownFeature");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should check each feature correctly")
        void shouldCheckEachFeatureCorrectly() {
            preferences.setStreaksEnabled(false);
            preferences.setAchievementsEnabled(false);
            preferences.setAnalyticsEnabled(true);
            when(featurePreferencesRepository.findByUser(user)).thenReturn(Optional.of(preferences));

            assertThat(featureToggleService.isFeatureEnabled(user, "streaks")).isFalse();
            assertThat(featureToggleService.isFeatureEnabled(user, "achievements")).isFalse();
            assertThat(featureToggleService.isFeatureEnabled(user, "analytics")).isTrue();
            assertThat(featureToggleService.isFeatureEnabled(user, "flightPlan")).isTrue();
            assertThat(featureToggleService.isFeatureEnabled(user, "progressReminders")).isTrue();
            assertThat(featureToggleService.isFeatureEnabled(user, "milestones")).isTrue();
            assertThat(featureToggleService.isFeatureEnabled(user, "obstacleTracking")).isTrue();
            assertThat(featureToggleService.isFeatureEnabled(user, "reflections")).isTrue();
            assertThat(featureToggleService.isFeatureEnabled(user, "socialProof")).isTrue();
        }
    }

    @Nested
    @DisplayName("getPreferences tests")
    class GetPreferencesTests {

        @Test
        @DisplayName("Should return existing preferences")
        void shouldReturnExistingPreferences() {
            when(featurePreferencesRepository.findByUser(user)).thenReturn(Optional.of(preferences));

            var response = featureToggleService.getPreferences(user);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getDailyJournalEnabled()).isTrue();
            assertThat(response.getStreaksEnabled()).isTrue();
            verify(featurePreferencesRepository).findByUser(user);
            verify(featurePreferencesRepository, never()).save(any(FeaturePreferences.class));
        }

        @Test
        @DisplayName("Should create default preferences when none exist")
        void shouldCreateDefaultPreferencesWhenNoneExist() {
            when(featurePreferencesRepository.findByUser(user)).thenReturn(Optional.empty());
            when(featurePreferencesRepository.save(any(FeaturePreferences.class))).thenReturn(preferences);

            var response = featureToggleService.getPreferences(user);

            assertThat(response).isNotNull();
            verify(featurePreferencesRepository).save(any(FeaturePreferences.class));
        }
    }

    @Nested
    @DisplayName("updatePreferences tests")
    class UpdatePreferencesTests {

        @Test
        @DisplayName("Should update existing preferences")
        void shouldUpdateExistingPreferences() {
            var request = FeaturePreferencesRequest.builder()
                    .dailyJournalEnabled(false)
                    .streaksEnabled(false)
                    .achievementsEnabled(true)
                    .build();

            when(featurePreferencesRepository.findByUser(user)).thenReturn(Optional.of(preferences));
            when(featurePreferencesRepository.save(any(FeaturePreferences.class))).thenReturn(preferences);

            var response = featureToggleService.updatePreferences(user, request);

            assertThat(response).isNotNull();
            verify(featurePreferencesRepository).save(any(FeaturePreferences.class));
        }

        @Test
        @DisplayName("Should create and update preferences when none exist")
        void shouldCreateAndUpdatePreferencesWhenNoneExist() {
            var request = FeaturePreferencesRequest.builder()
                    .dailyJournalEnabled(false)
                    .build();

            when(featurePreferencesRepository.findByUser(user)).thenReturn(Optional.empty());
            when(featurePreferencesRepository.save(any(FeaturePreferences.class))).thenReturn(preferences);

            var response = featureToggleService.updatePreferences(user, request);

            assertThat(response).isNotNull();
            verify(featurePreferencesRepository, times(2)).save(any(FeaturePreferences.class));
        }

        @Test
        @DisplayName("Should only update provided fields (partial update)")
        void shouldOnlyUpdateProvidedFields() {
            var request = FeaturePreferencesRequest.builder()
                    .dailyJournalEnabled(false)
                    .build();

            when(featurePreferencesRepository.findByUser(user)).thenReturn(Optional.of(preferences));
            when(featurePreferencesRepository.save(any(FeaturePreferences.class))).thenAnswer(invocation -> {
                var saved = invocation.getArgument(0, FeaturePreferences.class);
                assertThat(saved.getDailyJournalEnabled()).isFalse();
                assertThat(saved.getStreaksEnabled()).isTrue();
                assertThat(saved.getAchievementsEnabled()).isTrue();
                return saved;
            });

            featureToggleService.updatePreferences(user, request);

            verify(featurePreferencesRepository).save(any(FeaturePreferences.class));
        }
    }
}
