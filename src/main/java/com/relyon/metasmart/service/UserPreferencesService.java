package com.relyon.metasmart.service;

import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.entity.user.UserPreferences;
import com.relyon.metasmart.entity.user.dto.UserPreferencesRequest;
import com.relyon.metasmart.entity.user.dto.UserPreferencesResponse;
import com.relyon.metasmart.repository.UserPreferencesRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPreferencesService {

    private final UserPreferencesRepository userPreferencesRepository;

    @Transactional
    public UserPreferencesResponse getPreferences(User user) {
        log.debug("Getting preferences for user: {}", user.getEmail());

        var preferences = userPreferencesRepository.findByUser(user)
                .orElseGet(() -> createDefaultPreferences(user));

        return mapToResponse(preferences);
    }

    @Transactional
    public UserPreferencesResponse updatePreferences(User user, UserPreferencesRequest request) {
        log.debug("Updating preferences for user: {}", user.getEmail());

        var preferences = userPreferencesRepository.findByUser(user)
                .orElseGet(() -> createDefaultPreferences(user));

        Optional.ofNullable(request.getTimezone()).ifPresent(preferences::setTimezone);
        Optional.ofNullable(request.getLanguage()).ifPresent(preferences::setLanguage);
        Optional.ofNullable(request.getEmailNotifications()).ifPresent(preferences::setEmailNotifications);
        Optional.ofNullable(request.getPushNotifications()).ifPresent(preferences::setPushNotifications);
        Optional.ofNullable(request.getWeeklyDigest()).ifPresent(preferences::setWeeklyDigest);
        Optional.ofNullable(request.getStreakReminders()).ifPresent(preferences::setStreakReminders);
        Optional.ofNullable(request.getGuardianNudges()).ifPresent(preferences::setGuardianNudges);
        Optional.ofNullable(request.getPreferredReminderTime()).ifPresent(preferences::setPreferredReminderTime);

        var savedPreferences = userPreferencesRepository.save(preferences);
        log.info("Preferences updated for user: {}", user.getEmail());

        return mapToResponse(savedPreferences);
    }

    private UserPreferences createDefaultPreferences(User user) {
        log.debug("Creating default preferences for user: {}", user.getEmail());

        var preferences = UserPreferences.builder()
                .user(user)
                .build();

        return userPreferencesRepository.save(preferences);
    }

    private UserPreferencesResponse mapToResponse(UserPreferences preferences) {
        return UserPreferencesResponse.builder()
                .id(preferences.getId())
                .timezone(preferences.getTimezone())
                .language(preferences.getLanguage())
                .emailNotifications(preferences.getEmailNotifications())
                .pushNotifications(preferences.getPushNotifications())
                .weeklyDigest(preferences.getWeeklyDigest())
                .streakReminders(preferences.getStreakReminders())
                .guardianNudges(preferences.getGuardianNudges())
                .preferredReminderTime(preferences.getPreferredReminderTime())
                .build();
    }
}
