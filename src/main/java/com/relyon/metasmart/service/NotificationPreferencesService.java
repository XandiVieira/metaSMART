package com.relyon.metasmart.service;

import com.relyon.metasmart.entity.notification.NotificationPreferences;
import com.relyon.metasmart.entity.notification.dto.NotificationPreferencesRequest;
import com.relyon.metasmart.entity.notification.dto.NotificationPreferencesResponse;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.repository.NotificationPreferencesRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPreferencesService {

    private final NotificationPreferencesRepository notificationPreferencesRepository;

    @Transactional(readOnly = true)
    public NotificationPreferencesResponse getPreferences(User user) {
        log.debug("Getting notification preferences for user: {}", user.getEmail());

        var preferences = notificationPreferencesRepository.findByUser(user)
                .orElseGet(() -> createDefaultPreferences(user));

        return mapToResponse(preferences);
    }

    @Transactional
    public NotificationPreferencesResponse updatePreferences(User user, NotificationPreferencesRequest request) {
        log.debug("Updating notification preferences for user: {}", user.getEmail());

        var preferences = notificationPreferencesRepository.findByUser(user)
                .orElseGet(() -> createDefaultPreferences(user));

        // Push settings
        Optional.ofNullable(request.getPushEnabled()).ifPresent(preferences::setPushEnabled);
        Optional.ofNullable(request.getPushGoalReminders()).ifPresent(preferences::setPushGoalReminders);
        Optional.ofNullable(request.getPushProgressReminders()).ifPresent(preferences::setPushProgressReminders);
        Optional.ofNullable(request.getPushMilestones()).ifPresent(preferences::setPushMilestones);
        Optional.ofNullable(request.getPushStreakAlerts()).ifPresent(preferences::setPushStreakAlerts);
        Optional.ofNullable(request.getPushGuardianNudges()).ifPresent(preferences::setPushGuardianNudges);

        // Email settings
        Optional.ofNullable(request.getEmailEnabled()).ifPresent(preferences::setEmailEnabled);
        Optional.ofNullable(request.getEmailWeeklyDigest()).ifPresent(preferences::setEmailWeeklyDigest);
        Optional.ofNullable(request.getEmailMilestones()).ifPresent(preferences::setEmailMilestones);
        Optional.ofNullable(request.getEmailStreakAtRisk()).ifPresent(preferences::setEmailStreakAtRisk);

        // WhatsApp settings
        Optional.ofNullable(request.getWhatsappEnabled()).ifPresent(preferences::setWhatsappEnabled);
        Optional.ofNullable(request.getWhatsappNumber()).ifPresent(preferences::setWhatsappNumber);

        // Quiet hours
        Optional.ofNullable(request.getQuietHoursEnabled()).ifPresent(preferences::setQuietHoursEnabled);
        Optional.ofNullable(request.getQuietHoursStart()).ifPresent(preferences::setQuietHoursStart);
        Optional.ofNullable(request.getQuietHoursEnd()).ifPresent(preferences::setQuietHoursEnd);

        var savedPreferences = notificationPreferencesRepository.save(preferences);
        log.info("Notification preferences updated for user: {}", user.getEmail());

        return mapToResponse(savedPreferences);
    }

    private NotificationPreferences createDefaultPreferences(User user) {
        log.debug("Creating default notification preferences for user: {}", user.getEmail());

        var preferences = NotificationPreferences.builder()
                .user(user)
                .build();

        return notificationPreferencesRepository.save(preferences);
    }

    private NotificationPreferencesResponse mapToResponse(NotificationPreferences preferences) {
        return NotificationPreferencesResponse.builder()
                .id(preferences.getId())
                .pushEnabled(preferences.getPushEnabled())
                .pushGoalReminders(preferences.getPushGoalReminders())
                .pushProgressReminders(preferences.getPushProgressReminders())
                .pushMilestones(preferences.getPushMilestones())
                .pushStreakAlerts(preferences.getPushStreakAlerts())
                .pushGuardianNudges(preferences.getPushGuardianNudges())
                .emailEnabled(preferences.getEmailEnabled())
                .emailWeeklyDigest(preferences.getEmailWeeklyDigest())
                .emailMilestones(preferences.getEmailMilestones())
                .emailStreakAtRisk(preferences.getEmailStreakAtRisk())
                .whatsappEnabled(preferences.getWhatsappEnabled())
                .whatsappNumber(preferences.getWhatsappNumber())
                .quietHoursEnabled(preferences.getQuietHoursEnabled())
                .quietHoursStart(preferences.getQuietHoursStart())
                .quietHoursEnd(preferences.getQuietHoursEnd())
                .build();
    }
}
