package com.relyon.metasmart.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.relyon.metasmart.config.CorsConfig;
import com.relyon.metasmart.config.JwtService;
import com.relyon.metasmart.config.RateLimitConfig;
import com.relyon.metasmart.config.SecurityConfig;
import com.relyon.metasmart.entity.actionplan.CompletionStatus;
import com.relyon.metasmart.entity.history.dto.*;
import com.relyon.metasmart.entity.journal.Mood;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.GlobalExceptionHandler;
import com.relyon.metasmart.service.ActivityHistoryService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ActivityHistoryController.class)
@Import({SecurityConfig.class, CorsConfig.class, RateLimitConfig.class, GlobalExceptionHandler.class})
class ActivityHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @MockitoBean
    private ActivityHistoryService activityHistoryService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private User user;
    private ActivityHistoryResponse historyResponse;
    private DailyActivityResponse dailyActivityResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John")
                .email("john@test.com")
                .password("password")
                .build();

        var taskSummary = TaskCompletionSummary.builder()
                .id(1L)
                .actionItemId(1L)
                .actionItemTitle("Study vocabulary")
                .goalId(1L)
                .goalTitle("Learn Spanish")
                .status(CompletionStatus.COMPLETED)
                .completedAt(LocalDateTime.now())
                .build();

        var progressSummary = ProgressEntrySummary.builder()
                .id(1L)
                .goalId(1L)
                .goalTitle("Learn Spanish")
                .progressValue(BigDecimal.valueOf(2))
                .unit("hours")
                .percentageOfGoal(BigDecimal.valueOf(2))
                .createdAt(LocalDateTime.now())
                .build();

        var journalSummary = JournalEntrySummary.builder()
                .id(1L)
                .content("Great progress today")
                .mood(Mood.GOOD)
                .shieldUsed(false)
                .createdAt(LocalDateTime.now())
                .build();

        dailyActivityResponse = DailyActivityResponse.builder()
                .date(LocalDate.now())
                .taskCompletions(List.of(taskSummary))
                .progressEntries(List.of(progressSummary))
                .journalEntry(journalSummary)
                .hasActivity(true)
                .hasRealActivity(true)
                .protectedByShield(false)
                .build();

        var dailyActivities = new LinkedHashMap<LocalDate, DailyActivityResponse>();
        dailyActivities.put(LocalDate.now(), dailyActivityResponse);

        var summary = ActivityHistoryResponse.ActivitySummary.builder()
                .totalTaskCompletions(1)
                .totalProgressEntries(1)
                .totalJournalEntries(1)
                .build();

        historyResponse = ActivityHistoryResponse.builder()
                .startDate(LocalDate.now().minusDays(7))
                .endDate(LocalDate.now())
                .totalDays(8)
                .activeDays(3)
                .dailyActivities(dailyActivities)
                .summary(summary)
                .build();
    }

    @Nested
    @DisplayName("Get activity history tests")
    class GetActivityHistoryTests {

        @Test
        @DisplayName("Should get activity history")
        void shouldGetActivityHistory() throws Exception {
            var startDate = LocalDate.now().minusDays(7);
            var endDate = LocalDate.now();

            when(activityHistoryService.getActivityHistory(any(User.class), eq(startDate), eq(endDate)))
                    .thenReturn(historyResponse);

            mockMvc.perform(get("/api/v1/history")
                            .with(user(user))
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalDays").value(8))
                    .andExpect(jsonPath("$.activeDays").value(3))
                    .andExpect(jsonPath("$.summary.totalTaskCompletions").value(1))
                    .andExpect(jsonPath("$.summary.totalProgressEntries").value(1))
                    .andExpect(jsonPath("$.summary.totalJournalEntries").value(1));
        }

        @Test
        @DisplayName("Should return error when start date is missing")
        void shouldReturnErrorWhenStartDateIsMissing() throws Exception {
            mockMvc.perform(get("/api/v1/history")
                            .with(user(user))
                            .param("endDate", LocalDate.now().toString()))
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("Should return error when end date is missing")
        void shouldReturnErrorWhenEndDateIsMissing() throws Exception {
            mockMvc.perform(get("/api/v1/history")
                            .with(user(user))
                            .param("startDate", LocalDate.now().minusDays(7).toString()))
                    .andExpect(status().is5xxServerError());
        }
    }

    @Nested
    @DisplayName("Get daily activity tests")
    class GetDailyActivityTests {

        @Test
        @DisplayName("Should get daily activity")
        void shouldGetDailyActivity() throws Exception {
            var date = LocalDate.now();

            when(activityHistoryService.getDailyActivity(any(User.class), eq(date)))
                    .thenReturn(dailyActivityResponse);

            mockMvc.perform(get("/api/v1/history/date/" + date)
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.hasActivity").value(true))
                    .andExpect(jsonPath("$.hasRealActivity").value(true))
                    .andExpect(jsonPath("$.protectedByShield").value(false))
                    .andExpect(jsonPath("$.taskCompletions[0].actionItemTitle").value("Study vocabulary"))
                    .andExpect(jsonPath("$.progressEntries[0].goalTitle").value("Learn Spanish"))
                    .andExpect(jsonPath("$.journalEntry.content").value("Great progress today"));
        }

        @Test
        @DisplayName("Should get daily activity with shield protection")
        void shouldGetDailyActivityWithShieldProtection() throws Exception {
            var date = LocalDate.now();
            dailyActivityResponse.setHasRealActivity(false);
            dailyActivityResponse.setProtectedByShield(true);
            dailyActivityResponse.getJournalEntry().setShieldUsed(true);

            when(activityHistoryService.getDailyActivity(any(User.class), eq(date)))
                    .thenReturn(dailyActivityResponse);

            mockMvc.perform(get("/api/v1/history/date/" + date)
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.hasActivity").value(true))
                    .andExpect(jsonPath("$.hasRealActivity").value(false))
                    .andExpect(jsonPath("$.protectedByShield").value(true))
                    .andExpect(jsonPath("$.journalEntry.shieldUsed").value(true));
        }
    }
}
