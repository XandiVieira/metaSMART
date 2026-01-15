package com.relyon.metasmart.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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
import com.relyon.metasmart.entity.reflection.ReflectionRating;
import com.relyon.metasmart.entity.reflection.dto.PendingReflectionResponse;
import com.relyon.metasmart.entity.reflection.dto.ReflectionRequest;
import com.relyon.metasmart.entity.reflection.dto.ReflectionResponse;
import com.relyon.metasmart.entity.reflection.dto.ReflectionStatusResponse;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.GlobalExceptionHandler;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.service.ReflectionService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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

@WebMvcTest(ReflectionController.class)
@Import({SecurityConfig.class, CorsConfig.class, RateLimitConfig.class, GlobalExceptionHandler.class})
class ReflectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @MockitoBean
    private ReflectionService reflectionService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private User user;
    private ReflectionResponse reflectionResponse;
    private ReflectionRequest reflectionRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John")
                .email("john@test.com")
                .password("password")
                .build();

        reflectionResponse = ReflectionResponse.builder()
                .id(1L)
                .goalId(1L)
                .goalTitle("Run 5km")
                .periodStart(LocalDate.now().minusDays(7))
                .periodEnd(LocalDate.now())
                .rating(ReflectionRating.GOOD)
                .wentWell("Made good progress")
                .challenges("Time management")
                .adjustments("Wake up earlier")
                .moodNote("Feeling motivated")
                .willContinue(true)
                .motivationLevel(8)
                .createdAt(LocalDateTime.now())
                .build();

        reflectionRequest = ReflectionRequest.builder()
                .rating(ReflectionRating.GOOD)
                .wentWell("Made good progress")
                .challenges("Time management")
                .adjustments("Wake up earlier")
                .moodNote("Feeling motivated")
                .willContinue(true)
                .motivationLevel(8)
                .build();
    }

    @Nested
    @DisplayName("Get pending reflections tests")
    class GetPendingReflectionsTests {

        @Test
        @DisplayName("Should get pending reflections successfully")
        void shouldGetPendingReflectionsSuccessfully() throws Exception {
            var pendingReflection = PendingReflectionResponse.builder()
                    .goalId(1L)
                    .goalTitle("Run 5km")
                    .periodStart(LocalDate.now().minusDays(7))
                    .periodEnd(LocalDate.now())
                    .daysOverdue(2)
                    .build();

            when(reflectionService.getPendingReflections(any(User.class)))
                    .thenReturn(List.of(pendingReflection));

            mockMvc.perform(get("/api/v1/goals/reflections/pending")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].goalId").value(1L))
                    .andExpect(jsonPath("$[0].goalTitle").value("Run 5km"));

            verify(reflectionService).getPendingReflections(any(User.class));
        }

        @Test
        @DisplayName("Should return empty list when no pending reflections")
        void shouldReturnEmptyListWhenNoPendingReflections() throws Exception {
            when(reflectionService.getPendingReflections(any(User.class)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/v1/goals/reflections/pending")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("Should return 403 when not authenticated")
        void shouldReturn403WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/goals/reflections/pending"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Get reflection status tests")
    class GetReflectionStatusTests {

        @Test
        @DisplayName("Should get reflection status successfully")
        void shouldGetReflectionStatusSuccessfully() throws Exception {
            var statusResponse = ReflectionStatusResponse.builder()
                    .goalId(1L)
                    .reflectionDue(true)
                    .currentPeriodStart(LocalDate.now().minusDays(7))
                    .currentPeriodEnd(LocalDate.now())
                    .totalReflections(5)
                    .build();

            when(reflectionService.getReflectionStatus(eq(1L), any(User.class)))
                    .thenReturn(statusResponse);

            mockMvc.perform(get("/api/v1/goals/1/reflections/status")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.goalId").value(1L))
                    .andExpect(jsonPath("$.reflectionDue").value(true))
                    .andExpect(jsonPath("$.totalReflections").value(5));

            verify(reflectionService).getReflectionStatus(eq(1L), any(User.class));
        }

        @Test
        @DisplayName("Should return 404 when goal not found")
        void shouldReturn404WhenGoalNotFound() throws Exception {
            when(reflectionService.getReflectionStatus(eq(999L), any(User.class)))
                    .thenThrow(new ResourceNotFoundException("Goal not found"));

            mockMvc.perform(get("/api/v1/goals/999/reflections/status")
                            .with(user(user)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Create reflection tests")
    class CreateReflectionTests {

        @Test
        @DisplayName("Should create reflection successfully")
        void shouldCreateReflectionSuccessfully() throws Exception {
            when(reflectionService.createReflection(eq(1L), any(ReflectionRequest.class), any(User.class)))
                    .thenReturn(reflectionResponse);

            mockMvc.perform(post("/api/v1/goals/1/reflections")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reflectionRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.rating").value("GOOD"))
                    .andExpect(jsonPath("$.wentWell").value("Made good progress"));

            verify(reflectionService).createReflection(eq(1L), any(ReflectionRequest.class), any(User.class));
        }

        @Test
        @DisplayName("Should return 400 when rating is missing")
        void shouldReturn400WhenRatingMissing() throws Exception {
            var invalidRequest = ReflectionRequest.builder()
                    .wentWell("Progress")
                    .build();

            mockMvc.perform(post("/api/v1/goals/1/reflections")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when motivation level is out of range")
        void shouldReturn400WhenMotivationLevelOutOfRange() throws Exception {
            var invalidRequest = ReflectionRequest.builder()
                    .rating(ReflectionRating.GOOD)
                    .motivationLevel(15)
                    .build();

            mockMvc.perform(post("/api/v1/goals/1/reflections")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 when goal not found")
        void shouldReturn404WhenGoalNotFound() throws Exception {
            when(reflectionService.createReflection(eq(999L), any(ReflectionRequest.class), any(User.class)))
                    .thenThrow(new ResourceNotFoundException("Goal not found"));

            mockMvc.perform(post("/api/v1/goals/999/reflections")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reflectionRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should create reflection with all ratings")
        void shouldCreateReflectionWithAllRatings() throws Exception {
            for (ReflectionRating rating : ReflectionRating.values()) {
                var request = ReflectionRequest.builder()
                        .rating(rating)
                        .build();

                var response = ReflectionResponse.builder()
                        .id(1L)
                        .rating(rating)
                        .build();

                when(reflectionService.createReflection(eq(1L), any(ReflectionRequest.class), any(User.class)))
                        .thenReturn(response);

                mockMvc.perform(post("/api/v1/goals/1/reflections")
                                .with(user(user))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.rating").value(rating.name()));
            }
        }
    }

    @Nested
    @DisplayName("Get reflection history tests")
    class GetReflectionHistoryTests {

        @Test
        @DisplayName("Should get reflection history successfully")
        void shouldGetReflectionHistorySuccessfully() throws Exception {
            var page = new PageImpl<>(List.of(reflectionResponse));

            when(reflectionService.getReflectionHistory(eq(1L), any(User.class), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/goals/1/reflections")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(1L))
                    .andExpect(jsonPath("$.content[0].rating").value("GOOD"));

            verify(reflectionService).getReflectionHistory(eq(1L), any(User.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should get reflection history with pagination")
        void shouldGetReflectionHistoryWithPagination() throws Exception {
            var page = new PageImpl<>(List.of(reflectionResponse));

            when(reflectionService.getReflectionHistory(eq(1L), any(User.class), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/goals/1/reflections")
                            .with(user(user))
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should return empty page when no reflections")
        void shouldReturnEmptyPageWhenNoReflections() throws Exception {
            var emptyPage = new PageImpl<ReflectionResponse>(List.of());

            when(reflectionService.getReflectionHistory(eq(1L), any(User.class), any(Pageable.class)))
                    .thenReturn(emptyPage);

            mockMvc.perform(get("/api/v1/goals/1/reflections")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }
    }

    @Nested
    @DisplayName("Get specific reflection tests")
    class GetSpecificReflectionTests {

        @Test
        @DisplayName("Should get specific reflection successfully")
        void shouldGetSpecificReflectionSuccessfully() throws Exception {
            when(reflectionService.getReflection(eq(1L), eq(1L), any(User.class)))
                    .thenReturn(reflectionResponse);

            mockMvc.perform(get("/api/v1/goals/1/reflections/1")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.goalId").value(1L))
                    .andExpect(jsonPath("$.rating").value("GOOD"));

            verify(reflectionService).getReflection(eq(1L), eq(1L), any(User.class));
        }

        @Test
        @DisplayName("Should return 404 when reflection not found")
        void shouldReturn404WhenReflectionNotFound() throws Exception {
            when(reflectionService.getReflection(eq(1L), eq(999L), any(User.class)))
                    .thenThrow(new ResourceNotFoundException("Reflection not found"));

            mockMvc.perform(get("/api/v1/goals/1/reflections/999")
                            .with(user(user)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 when goal not found")
        void shouldReturn404WhenGoalNotFoundForReflection() throws Exception {
            when(reflectionService.getReflection(eq(999L), eq(1L), any(User.class)))
                    .thenThrow(new ResourceNotFoundException("Goal not found"));

            mockMvc.perform(get("/api/v1/goals/999/reflections/1")
                            .with(user(user)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Update reflection tests")
    class UpdateReflectionTests {

        @Test
        @DisplayName("Should update reflection successfully")
        void shouldUpdateReflectionSuccessfully() throws Exception {
            var updatedResponse = ReflectionResponse.builder()
                    .id(1L)
                    .goalId(1L)
                    .rating(ReflectionRating.EXCELLENT)
                    .wentWell("Even better progress")
                    .willContinue(true)
                    .motivationLevel(10)
                    .build();

            when(reflectionService.updateReflection(eq(1L), eq(1L), any(ReflectionRequest.class), any(User.class)))
                    .thenReturn(updatedResponse);

            var updateRequest = ReflectionRequest.builder()
                    .rating(ReflectionRating.EXCELLENT)
                    .wentWell("Even better progress")
                    .motivationLevel(10)
                    .build();

            mockMvc.perform(put("/api/v1/goals/1/reflections/1")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rating").value("EXCELLENT"))
                    .andExpect(jsonPath("$.wentWell").value("Even better progress"));

            verify(reflectionService).updateReflection(eq(1L), eq(1L), any(ReflectionRequest.class), any(User.class));
        }

        @Test
        @DisplayName("Should return 400 when update request is invalid")
        void shouldReturn400WhenUpdateRequestInvalid() throws Exception {
            var invalidRequest = ReflectionRequest.builder()
                    .motivationLevel(15)
                    .build();

            mockMvc.perform(put("/api/v1/goals/1/reflections/1")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 when reflection not found for update")
        void shouldReturn404WhenReflectionNotFoundForUpdate() throws Exception {
            when(reflectionService.updateReflection(eq(1L), eq(999L), any(ReflectionRequest.class), any(User.class)))
                    .thenThrow(new ResourceNotFoundException("Reflection not found"));

            mockMvc.perform(put("/api/v1/goals/1/reflections/999")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reflectionRequest)))
                    .andExpect(status().isNotFound());
        }
    }
}
