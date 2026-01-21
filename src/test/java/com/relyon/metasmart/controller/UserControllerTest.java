package com.relyon.metasmart.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.relyon.metasmart.config.CorsConfig;
import com.relyon.metasmart.config.JwtService;
import com.relyon.metasmart.config.RateLimitConfig;
import com.relyon.metasmart.config.SecurityConfig;
import com.relyon.metasmart.entity.feature.dto.FeaturePreferencesRequest;
import com.relyon.metasmart.entity.feature.dto.FeaturePreferencesResponse;
import com.relyon.metasmart.entity.notification.dto.NotificationPreferencesRequest;
import com.relyon.metasmart.entity.notification.dto.NotificationPreferencesResponse;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.entity.user.dto.UpdateProfileRequest;
import com.relyon.metasmart.entity.user.dto.UserPreferencesRequest;
import com.relyon.metasmart.entity.user.dto.UserPreferencesResponse;
import com.relyon.metasmart.entity.user.dto.UserProfileResponse;
import com.relyon.metasmart.exception.GlobalExceptionHandler;
import com.relyon.metasmart.service.FeatureToggleService;
import com.relyon.metasmart.service.NotificationPreferencesService;
import com.relyon.metasmart.service.UserPreferencesService;
import com.relyon.metasmart.service.UserProfileService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, CorsConfig.class, RateLimitConfig.class, GlobalExceptionHandler.class})
class UserControllerTest {

    private static final String BASE_URL = "/api/v1/users";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @MockitoBean
    private UserProfileService userProfileService;

    @MockitoBean
    private UserPreferencesService userPreferencesService;

    @MockitoBean
    private NotificationPreferencesService notificationPreferencesService;

    @MockitoBean
    private FeatureToggleService featureToggleService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private User user;
    private UserProfileResponse profileResponse;
    private UserPreferencesResponse preferencesResponse;
    private NotificationPreferencesResponse notificationPreferencesResponse;
    private FeaturePreferencesResponse featurePreferencesResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John")
                .email("john@test.com")
                .password("password")
                .streakShields(3)
                .build();

        profileResponse = UserProfileResponse.builder()
                .id(1L)
                .name("John")
                .email("john@test.com")
                .joinedAt(LocalDateTime.now().minusDays(30))
                .totalGoals(10L)
                .completedGoals(5L)
                .streakShields(3)
                .build();

        preferencesResponse = UserPreferencesResponse.builder()
                .id(1L)
                .timezone("America/Sao_Paulo")
                .language("en")
                .emailNotifications(true)
                .pushNotifications(true)
                .weeklyDigest(true)
                .streakReminders(true)
                .guardianNudges(true)
                .preferredReminderTime("09:00")
                .build();

        notificationPreferencesResponse = NotificationPreferencesResponse.builder()
                .id(1L)
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
                .quietHoursEnabled(true)
                .quietHoursStart("22:00")
                .quietHoursEnd("08:00")
                .build();

        featurePreferencesResponse = FeaturePreferencesResponse.builder()
                .id(1L)
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
    @DisplayName("Profile tests")
    class ProfileTests {

        @Test
        @DisplayName("Should get user profile")
        void shouldGetUserProfile() throws Exception {
            when(userProfileService.getProfile(any(User.class))).thenReturn(profileResponse);

            mockMvc.perform(get(BASE_URL + "/profile")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("John"))
                    .andExpect(jsonPath("$.email").value("john@test.com"))
                    .andExpect(jsonPath("$.totalGoals").value(10L))
                    .andExpect(jsonPath("$.completedGoals").value(5L))
                    .andExpect(jsonPath("$.streakShields").value(3));
        }

        @Test
        @DisplayName("Should update user profile")
        void shouldUpdateUserProfile() throws Exception {
            var request = UpdateProfileRequest.builder()
                    .name("John Updated")
                    .build();

            var updatedResponse = UserProfileResponse.builder()
                    .id(1L)
                    .name("John Updated")
                    .email("john@test.com")
                    .joinedAt(LocalDateTime.now().minusDays(30))
                    .totalGoals(10L)
                    .completedGoals(5L)
                    .streakShields(3)
                    .build();

            when(userProfileService.updateProfile(any(User.class), any(UpdateProfileRequest.class)))
                    .thenReturn(updatedResponse);

            mockMvc.perform(put(BASE_URL + "/profile")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("John Updated"));
        }
    }

    @Nested
    @DisplayName("Streak shield tests")
    class StreakShieldTests {

        @Test
        @DisplayName("Should use streak shield successfully")
        void shouldUseStreakShieldSuccessfully() throws Exception {
            when(userProfileService.useStreakShield(any(User.class))).thenReturn(true);

            mockMvc.perform(post(BASE_URL + "/streak-shields/use")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Streak shield used successfully"));
        }

        @Test
        @DisplayName("Should fail when no streak shields available")
        void shouldFailWhenNoStreakShieldsAvailable() throws Exception {
            when(userProfileService.useStreakShield(any(User.class))).thenReturn(false);

            mockMvc.perform(post(BASE_URL + "/streak-shields/use")
                            .with(user(user)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("No streak shields available"));
        }
    }

    @Nested
    @DisplayName("User preferences tests")
    class UserPreferencesTests {

        @Test
        @DisplayName("Should get user preferences")
        void shouldGetUserPreferences() throws Exception {
            when(userPreferencesService.getPreferences(any(User.class))).thenReturn(preferencesResponse);

            mockMvc.perform(get(BASE_URL + "/preferences")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.timezone").value("America/Sao_Paulo"))
                    .andExpect(jsonPath("$.language").value("en"))
                    .andExpect(jsonPath("$.emailNotifications").value(true))
                    .andExpect(jsonPath("$.pushNotifications").value(true))
                    .andExpect(jsonPath("$.preferredReminderTime").value("09:00"));
        }

        @Test
        @DisplayName("Should update user preferences")
        void shouldUpdateUserPreferences() throws Exception {
            var request = UserPreferencesRequest.builder()
                    .timezone("Europe/London")
                    .language("pt")
                    .emailNotifications(false)
                    .pushNotifications(true)
                    .weeklyDigest(false)
                    .streakReminders(true)
                    .guardianNudges(false)
                    .preferredReminderTime("10:00")
                    .build();

            var updatedResponse = UserPreferencesResponse.builder()
                    .id(1L)
                    .timezone("Europe/London")
                    .language("pt")
                    .emailNotifications(false)
                    .pushNotifications(true)
                    .weeklyDigest(false)
                    .streakReminders(true)
                    .guardianNudges(false)
                    .preferredReminderTime("10:00")
                    .build();

            when(userPreferencesService.updatePreferences(any(User.class), any(UserPreferencesRequest.class)))
                    .thenReturn(updatedResponse);

            mockMvc.perform(put(BASE_URL + "/preferences")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.timezone").value("Europe/London"))
                    .andExpect(jsonPath("$.language").value("pt"))
                    .andExpect(jsonPath("$.emailNotifications").value(false));
        }
    }

    @Nested
    @DisplayName("Notification preferences tests")
    class NotificationPreferencesTests {

        @Test
        @DisplayName("Should get notification preferences")
        void shouldGetNotificationPreferences() throws Exception {
            when(notificationPreferencesService.getPreferences(any(User.class)))
                    .thenReturn(notificationPreferencesResponse);

            mockMvc.perform(get(BASE_URL + "/notifications/preferences")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.pushEnabled").value(true))
                    .andExpect(jsonPath("$.pushGoalReminders").value(true))
                    .andExpect(jsonPath("$.emailEnabled").value(true))
                    .andExpect(jsonPath("$.emailWeeklyDigest").value(true))
                    .andExpect(jsonPath("$.whatsappEnabled").value(false))
                    .andExpect(jsonPath("$.quietHoursEnabled").value(true))
                    .andExpect(jsonPath("$.quietHoursStart").value("22:00"))
                    .andExpect(jsonPath("$.quietHoursEnd").value("08:00"));
        }

        @Test
        @DisplayName("Should update notification preferences")
        void shouldUpdateNotificationPreferences() throws Exception {
            var request = NotificationPreferencesRequest.builder()
                    .pushEnabled(false)
                    .pushGoalReminders(false)
                    .pushProgressReminders(false)
                    .pushMilestones(true)
                    .pushStreakAlerts(true)
                    .pushGuardianNudges(false)
                    .emailEnabled(true)
                    .emailWeeklyDigest(false)
                    .emailMilestones(true)
                    .emailStreakAtRisk(true)
                    .whatsappEnabled(true)
                    .whatsappNumber("+5511999999999")
                    .quietHoursEnabled(false)
                    .build();

            var updatedResponse = NotificationPreferencesResponse.builder()
                    .id(1L)
                    .pushEnabled(false)
                    .pushGoalReminders(false)
                    .pushProgressReminders(false)
                    .pushMilestones(true)
                    .pushStreakAlerts(true)
                    .pushGuardianNudges(false)
                    .emailEnabled(true)
                    .emailWeeklyDigest(false)
                    .emailMilestones(true)
                    .emailStreakAtRisk(true)
                    .whatsappEnabled(true)
                    .whatsappNumber("+5511999999999")
                    .quietHoursEnabled(false)
                    .build();

            when(notificationPreferencesService.updatePreferences(any(User.class), any(NotificationPreferencesRequest.class)))
                    .thenReturn(updatedResponse);

            mockMvc.perform(put(BASE_URL + "/notifications/preferences")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pushEnabled").value(false))
                    .andExpect(jsonPath("$.whatsappEnabled").value(true))
                    .andExpect(jsonPath("$.whatsappNumber").value("+5511999999999"))
                    .andExpect(jsonPath("$.quietHoursEnabled").value(false));
        }
    }

    @Nested
    @DisplayName("Feature preferences tests")
    class FeaturePreferencesTests {

        @Test
        @DisplayName("Should get feature preferences")
        void shouldGetFeaturePreferences() throws Exception {
            when(featureToggleService.getPreferences(any(User.class)))
                    .thenReturn(featurePreferencesResponse);

            mockMvc.perform(get(BASE_URL + "/features/preferences")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.dailyJournalEnabled").value(true))
                    .andExpect(jsonPath("$.streaksEnabled").value(true))
                    .andExpect(jsonPath("$.achievementsEnabled").value(true))
                    .andExpect(jsonPath("$.analyticsEnabled").value(true))
                    .andExpect(jsonPath("$.flightPlanEnabled").value(true))
                    .andExpect(jsonPath("$.progressRemindersEnabled").value(true))
                    .andExpect(jsonPath("$.milestonesEnabled").value(true))
                    .andExpect(jsonPath("$.obstacleTrackingEnabled").value(true))
                    .andExpect(jsonPath("$.reflectionsEnabled").value(true))
                    .andExpect(jsonPath("$.socialProofEnabled").value(true));
        }

        @Test
        @DisplayName("Should update feature preferences")
        void shouldUpdateFeaturePreferences() throws Exception {
            var request = FeaturePreferencesRequest.builder()
                    .dailyJournalEnabled(false)
                    .streaksEnabled(false)
                    .achievementsEnabled(true)
                    .analyticsEnabled(false)
                    .flightPlanEnabled(true)
                    .progressRemindersEnabled(false)
                    .milestonesEnabled(true)
                    .obstacleTrackingEnabled(false)
                    .reflectionsEnabled(true)
                    .socialProofEnabled(false)
                    .build();

            var updatedResponse = FeaturePreferencesResponse.builder()
                    .id(1L)
                    .dailyJournalEnabled(false)
                    .streaksEnabled(false)
                    .achievementsEnabled(true)
                    .analyticsEnabled(false)
                    .flightPlanEnabled(true)
                    .progressRemindersEnabled(false)
                    .milestonesEnabled(true)
                    .obstacleTrackingEnabled(false)
                    .reflectionsEnabled(true)
                    .socialProofEnabled(false)
                    .build();

            when(featureToggleService.updatePreferences(any(User.class), any(FeaturePreferencesRequest.class)))
                    .thenReturn(updatedResponse);

            mockMvc.perform(put(BASE_URL + "/features/preferences")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dailyJournalEnabled").value(false))
                    .andExpect(jsonPath("$.streaksEnabled").value(false))
                    .andExpect(jsonPath("$.achievementsEnabled").value(true))
                    .andExpect(jsonPath("$.analyticsEnabled").value(false))
                    .andExpect(jsonPath("$.socialProofEnabled").value(false));
        }
    }
}