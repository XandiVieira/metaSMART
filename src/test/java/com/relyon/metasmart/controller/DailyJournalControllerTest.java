package com.relyon.metasmart.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.relyon.metasmart.config.CorsConfig;
import com.relyon.metasmart.config.JwtService;
import com.relyon.metasmart.config.RateLimitConfig;
import com.relyon.metasmart.config.SecurityConfig;
import com.relyon.metasmart.entity.journal.Mood;
import com.relyon.metasmart.entity.journal.dto.DailyJournalRequest;
import com.relyon.metasmart.entity.journal.dto.DailyJournalResponse;
import com.relyon.metasmart.entity.journal.dto.UpdateDailyJournalRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.GlobalExceptionHandler;
import com.relyon.metasmart.service.DailyJournalService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DailyJournalController.class)
@Import({SecurityConfig.class, CorsConfig.class, RateLimitConfig.class, GlobalExceptionHandler.class})
class DailyJournalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @MockitoBean
    private DailyJournalService dailyJournalService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private User user;
    private DailyJournalResponse journalResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John")
                .email("john@test.com")
                .password("password")
                .build();

        journalResponse = DailyJournalResponse.builder()
                .id(1L)
                .journalDate(LocalDate.now())
                .content("Today was productive")
                .mood(Mood.GOOD)
                .shieldUsed(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Create journal entry tests")
    class CreateJournalEntryTests {

        @Test
        @DisplayName("Should create journal entry")
        void shouldCreateJournalEntry() throws Exception {
            var request = DailyJournalRequest.builder()
                    .journalDate(LocalDate.now())
                    .content("Today was productive")
                    .mood(Mood.GOOD)
                    .build();

            when(dailyJournalService.createJournalEntry(any(DailyJournalRequest.class), any(User.class)))
                    .thenReturn(journalResponse);

            mockMvc.perform(post("/api/v1/journal")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.content").value("Today was productive"))
                    .andExpect(jsonPath("$.mood").value("GOOD"));
        }

        @Test
        @DisplayName("Should return bad request when journal date is missing")
        void shouldReturnBadRequestWhenJournalDateIsMissing() throws Exception {
            var request = DailyJournalRequest.builder()
                    .content("Today was productive")
                    .mood(Mood.GOOD)
                    .build();

            mockMvc.perform(post("/api/v1/journal")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Get journal entry tests")
    class GetJournalEntryTests {

        @Test
        @DisplayName("Should get journal entry by ID")
        void shouldGetJournalEntryById() throws Exception {
            when(dailyJournalService.getJournalEntry(eq(1L), any(User.class)))
                    .thenReturn(journalResponse);

            mockMvc.perform(get("/api/v1/journal/1")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.content").value("Today was productive"));
        }

        @Test
        @DisplayName("Should get journal entry by date")
        void shouldGetJournalEntryByDate() throws Exception {
            var date = LocalDate.now();
            when(dailyJournalService.getJournalEntryByDate(eq(date), any(User.class)))
                    .thenReturn(Optional.of(journalResponse));

            mockMvc.perform(get("/api/v1/journal/date/" + date)
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L));
        }

        @Test
        @DisplayName("Should return not found when journal entry does not exist for date")
        void shouldReturnNotFoundWhenJournalEntryDoesNotExistForDate() throws Exception {
            var date = LocalDate.now();
            when(dailyJournalService.getJournalEntryByDate(eq(date), any(User.class)))
                    .thenReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/journal/date/" + date)
                            .with(user(user)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Get journal history tests")
    class GetJournalHistoryTests {

        @Test
        @DisplayName("Should get journal history")
        void shouldGetJournalHistory() throws Exception {
            var page = new PageImpl<>(List.of(journalResponse));
            when(dailyJournalService.getJournalHistory(any(User.class), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/journal")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(1L));
        }

        @Test
        @DisplayName("Should get journal entries by date range")
        void shouldGetJournalEntriesByDateRange() throws Exception {
            var startDate = LocalDate.now().minusDays(7);
            var endDate = LocalDate.now();
            when(dailyJournalService.getJournalEntriesByDateRange(any(User.class), eq(startDate), eq(endDate)))
                    .thenReturn(List.of(journalResponse));

            mockMvc.perform(get("/api/v1/journal/range")
                            .with(user(user))
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1L));
        }
    }

    @Nested
    @DisplayName("Update journal entry tests")
    class UpdateJournalEntryTests {

        @Test
        @DisplayName("Should update journal entry")
        void shouldUpdateJournalEntry() throws Exception {
            var request = UpdateDailyJournalRequest.builder()
                    .content("Updated content")
                    .mood(Mood.GREAT)
                    .build();

            when(dailyJournalService.updateJournalEntry(eq(1L), any(UpdateDailyJournalRequest.class), any(User.class)))
                    .thenReturn(journalResponse);

            mockMvc.perform(put("/api/v1/journal/1")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Delete journal entry tests")
    class DeleteJournalEntryTests {

        @Test
        @DisplayName("Should delete journal entry")
        void shouldDeleteJournalEntry() throws Exception {
            doNothing().when(dailyJournalService).deleteJournalEntry(eq(1L), any(User.class));

            mockMvc.perform(delete("/api/v1/journal/1")
                            .with(user(user)))
                    .andExpect(status().isNoContent());
        }
    }
}
