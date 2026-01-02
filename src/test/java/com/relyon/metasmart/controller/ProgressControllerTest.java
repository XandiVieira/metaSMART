package com.relyon.metasmart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.relyon.metasmart.config.CorsConfig;
import com.relyon.metasmart.config.RateLimitConfig;
import com.relyon.metasmart.config.SecurityConfig;
import com.relyon.metasmart.entity.progress.dto.*;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.GlobalExceptionHandler;
import com.relyon.metasmart.config.JwtService;
import com.relyon.metasmart.service.ProgressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProgressController.class)
@Import({SecurityConfig.class, CorsConfig.class, RateLimitConfig.class, GlobalExceptionHandler.class})
class ProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @MockitoBean
    private ProgressService progressService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private User user;
    private ProgressEntryResponse progressResponse;
    private MilestoneResponse milestoneResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John")
                .email("john@test.com")
                .password("password")
                .build();

        progressResponse = ProgressEntryResponse.builder()
                .id(1L)
                .progressValue(BigDecimal.ONE)
                .note("Good progress")
                .build();

        milestoneResponse = MilestoneResponse.builder()
                .id(1L)
                .percentage(50)
                .description("Halfway there!")
                .achieved(false)
                .build();
    }

    @Nested
    @DisplayName("Progress entry tests")
    class ProgressEntryTests {

        @Test
        @DisplayName("Should add progress entry")
        void shouldAddProgressEntry() throws Exception {
            var request = ProgressEntryRequest.builder()
                    .progressValue(BigDecimal.ONE)
                    .note("Good progress")
                    .build();

            when(progressService.addProgress(eq(1L), any(ProgressEntryRequest.class), any(User.class)))
                    .thenReturn(progressResponse);

            mockMvc.perform(post("/api/v1/goals/1/progress")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.progressValue").value(1));
        }

        @Test
        @DisplayName("Should get progress history")
        void shouldGetProgressHistory() throws Exception {
            var page = new PageImpl<>(List.of(progressResponse));
            when(progressService.getProgressHistory(eq(1L), any(User.class), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/goals/1/progress")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(1L));
        }

        @Test
        @DisplayName("Should update progress entry")
        void shouldUpdateProgressEntry() throws Exception {
            var request = UpdateProgressEntryRequest.builder()
                    .progressValue(BigDecimal.valueOf(2))
                    .build();

            when(progressService.updateProgressEntry(eq(1L), eq(1L), any(UpdateProgressEntryRequest.class), any(User.class)))
                    .thenReturn(progressResponse);

            mockMvc.perform(put("/api/v1/goals/1/progress/1")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should delete progress entry")
        void shouldDeleteProgressEntry() throws Exception {
            doNothing().when(progressService).deleteProgressEntry(eq(1L), eq(1L), any(User.class));

            mockMvc.perform(delete("/api/v1/goals/1/progress/1")
                            .with(user(user)))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("Milestone tests")
    class MilestoneTests {

        @Test
        @DisplayName("Should add milestone")
        void shouldAddMilestone() throws Exception {
            var request = MilestoneRequest.builder()
                    .percentage(50)
                    .description("Halfway there!")
                    .build();

            when(progressService.addMilestone(eq(1L), any(MilestoneRequest.class), any(User.class)))
                    .thenReturn(milestoneResponse);

            mockMvc.perform(post("/api/v1/goals/1/milestones")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.percentage").value(50));
        }

        @Test
        @DisplayName("Should get milestones")
        void shouldGetMilestones() throws Exception {
            when(progressService.getMilestones(eq(1L), any(User.class)))
                    .thenReturn(List.of(milestoneResponse));

            mockMvc.perform(get("/api/v1/goals/1/milestones")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].percentage").value(50));
        }

        @Test
        @DisplayName("Should delete milestone")
        void shouldDeleteMilestone() throws Exception {
            doNothing().when(progressService).deleteMilestone(eq(1L), eq(1L), any(User.class));

            mockMvc.perform(delete("/api/v1/goals/1/milestones/1")
                            .with(user(user)))
                    .andExpect(status().isNoContent());
        }
    }
}
