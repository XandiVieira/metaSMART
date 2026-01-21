package com.relyon.metasmart.service;

import com.relyon.metasmart.entity.feature.FeaturePreferences;
import com.relyon.metasmart.entity.feature.dto.FeaturePreferencesRequest;
import com.relyon.metasmart.entity.feature.dto.FeaturePreferencesResponse;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.repository.FeaturePreferencesRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureToggleService {

    private final FeaturePreferencesRepository featurePreferencesRepository;

    @Transactional(readOnly = true)
    public boolean isFeatureEnabled(User user, String featureName) {
        var preferences = featurePreferencesRepository.findByUser(user)
                .orElse(null);

        if (preferences == null) {
            return true;
        }

        return switch (featureName) {
            case "dailyJournal" -> preferences.getDailyJournalEnabled();
            case "streaks" -> preferences.getStreaksEnabled();
            case "achievements" -> preferences.getAchievementsEnabled();
            case "analytics" -> preferences.getAnalyticsEnabled();
            case "flightPlan" -> preferences.getFlightPlanEnabled();
            case "progressReminders" -> preferences.getProgressRemindersEnabled();
            case "milestones" -> preferences.getMilestonesEnabled();
            case "obstacleTracking" -> preferences.getObstacleTrackingEnabled();
            case "reflections" -> preferences.getReflectionsEnabled();
            case "socialProof" -> preferences.getSocialProofEnabled();
            default -> true;
        };
    }

    @Transactional
    public FeaturePreferencesResponse getPreferences(User user) {
        log.debug("Getting feature preferences for user: {}", user.getEmail());

        var preferences = featurePreferencesRepository.findByUser(user)
                .orElseGet(() -> createDefaultPreferences(user));

        return mapToResponse(preferences);
    }

    @Transactional
    public FeaturePreferencesResponse updatePreferences(User user, FeaturePreferencesRequest request) {
        log.debug("Updating feature preferences for user: {}", user.getEmail());

        var preferences = featurePreferencesRepository.findByUser(user)
                .orElseGet(() -> createDefaultPreferences(user));

        Optional.ofNullable(request.getDailyJournalEnabled()).ifPresent(preferences::setDailyJournalEnabled);
        Optional.ofNullable(request.getStreaksEnabled()).ifPresent(preferences::setStreaksEnabled);
        Optional.ofNullable(request.getAchievementsEnabled()).ifPresent(preferences::setAchievementsEnabled);
        Optional.ofNullable(request.getAnalyticsEnabled()).ifPresent(preferences::setAnalyticsEnabled);
        Optional.ofNullable(request.getFlightPlanEnabled()).ifPresent(preferences::setFlightPlanEnabled);
        Optional.ofNullable(request.getProgressRemindersEnabled()).ifPresent(preferences::setProgressRemindersEnabled);
        Optional.ofNullable(request.getMilestonesEnabled()).ifPresent(preferences::setMilestonesEnabled);
        Optional.ofNullable(request.getObstacleTrackingEnabled()).ifPresent(preferences::setObstacleTrackingEnabled);
        Optional.ofNullable(request.getReflectionsEnabled()).ifPresent(preferences::setReflectionsEnabled);
        Optional.ofNullable(request.getSocialProofEnabled()).ifPresent(preferences::setSocialProofEnabled);

        var savedPreferences = featurePreferencesRepository.save(preferences);
        log.info("Feature preferences updated for user: {}", user.getEmail());

        return mapToResponse(savedPreferences);
    }

    private FeaturePreferences createDefaultPreferences(User user) {
        log.debug("Creating default feature preferences for user: {}", user.getEmail());

        var preferences = FeaturePreferences.builder()
                .user(user)
                .build();

        return featurePreferencesRepository.save(preferences);
    }

    private FeaturePreferencesResponse mapToResponse(FeaturePreferences preferences) {
        return FeaturePreferencesResponse.builder()
                .id(preferences.getId())
                .dailyJournalEnabled(preferences.getDailyJournalEnabled())
                .streaksEnabled(preferences.getStreaksEnabled())
                .achievementsEnabled(preferences.getAchievementsEnabled())
                .analyticsEnabled(preferences.getAnalyticsEnabled())
                .flightPlanEnabled(preferences.getFlightPlanEnabled())
                .progressRemindersEnabled(preferences.getProgressRemindersEnabled())
                .milestonesEnabled(preferences.getMilestonesEnabled())
                .obstacleTrackingEnabled(preferences.getObstacleTrackingEnabled())
                .reflectionsEnabled(preferences.getReflectionsEnabled())
                .socialProofEnabled(preferences.getSocialProofEnabled())
                .build();
    }
}
