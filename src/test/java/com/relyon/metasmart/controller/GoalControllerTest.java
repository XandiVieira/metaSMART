package com.relyon.metasmart.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.goal.GoalStatus;
import com.relyon.metasmart.entity.goal.dto.GoalRequest;
import com.relyon.metasmart.entity.goal.dto.GoalResponse;
import com.relyon.metasmart.entity.goal.dto.UpdateGoalRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.GlobalExceptionHandler;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.service.GoalService;
import java.math.BigDecimal;
import java.time.LocalDate;
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

@WebMvcTest(GoalController.class)
@Import({SecurityConfig.class, CorsConfig.class, RateLimitConfig.class, GlobalExceptionHandler.class})
class GoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @MockitoBean
    private GoalService goalService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private User user;
    private GoalResponse goalResponse;
    private GoalRequest goalRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John")
                .email("john@test.com")
                .password("password")
                .build();

        goalResponse = GoalResponse.builder()
                .id(1L)
                .title("Run 5km")
                .description("Build endurance")
                .goalCategory(GoalCategory.HEALTH)
                .targetValue(new BigDecimal("5"))
                .unit("km")
                .currentProgress(BigDecimal.ZERO)
                .goalStatus(GoalStatus.ACTIVE)
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusMonths(3))
                .build();

        goalRequest = GoalRequest.builder()
                .title("Run 5km")
                .description("Build endurance")
                .goalCategory(GoalCategory.HEALTH)
                .targetValue(new BigDecimal("5"))
                .unit("km")
                .motivation("Improve health")
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusMonths(3))
                .build();
    }

    @Nested
    @DisplayName("Create goal tests")
    class CreateGoalTests {

        @Test
        @DisplayName("Should create goal successfully")
        void shouldCreateGoalSuccessfully() throws Exception {
            when(goalService.create(any(GoalRequest.class), any(User.class))).thenReturn(goalResponse);

            mockMvc.perform(post("/api/v1/goals")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(goalRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.title").value("Run 5km"));
        }

        @Test
        @DisplayName("Should return 403 when not authenticated")
        void shouldReturn403WhenNotAuthenticated() throws Exception {
            mockMvc.perform(post("/api/v1/goals")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(goalRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Get goal tests")
    class GetGoalTests {

        @Test
        @DisplayName("Should get goal by id")
        void shouldGetGoalById() throws Exception {
            when(goalService.findById(eq(1L), any(User.class))).thenReturn(goalResponse);

            mockMvc.perform(get("/api/v1/goals/1")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.title").value("Run 5km"));
        }

        @Test
        @DisplayName("Should return 404 when goal not found")
        void shouldReturn404WhenGoalNotFound() throws Exception {
            when(goalService.findById(eq(1L), any(User.class)))
                    .thenThrow(new ResourceNotFoundException("Goal not found"));

            mockMvc.perform(get("/api/v1/goals/1")
                            .with(user(user)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should get all goals")
        void shouldGetAllGoals() throws Exception {
            var page = new PageImpl<>(List.of(goalResponse));
            when(goalService.findAll(any(User.class), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/v1/goals")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(1L));
        }

        @Test
        @DisplayName("Should get goals by status")
        void shouldGetGoalsByStatus() throws Exception {
            var page = new PageImpl<>(List.of(goalResponse));
            when(goalService.findByStatus(any(User.class), eq(GoalStatus.ACTIVE), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/goals/status/ACTIVE")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].goalStatus").value("ACTIVE"));
        }

        @Test
        @DisplayName("Should get goals by category")
        void shouldGetGoalsByCategory() throws Exception {
            var page = new PageImpl<>(List.of(goalResponse));
            when(goalService.findByCategory(any(User.class), eq(GoalCategory.HEALTH), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/goals/category/HEALTH")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].goalCategory").value("HEALTH"));
        }
    }

    @Nested
    @DisplayName("Update goal tests")
    class UpdateGoalTests {

        @Test
        @DisplayName("Should update goal successfully")
        void shouldUpdateGoalSuccessfully() throws Exception {
            var updateRequest = UpdateGoalRequest.builder()
                    .title("Updated title")
                    .build();

            when(goalService.update(eq(1L), any(UpdateGoalRequest.class), any(User.class)))
                    .thenReturn(goalResponse);

            mockMvc.perform(put("/api/v1/goals/1")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L));
        }
    }

    @Nested
    @DisplayName("Delete goal tests")
    class DeleteGoalTests {

        @Test
        @DisplayName("Should delete goal successfully")
        void shouldDeleteGoalSuccessfully() throws Exception {
            doNothing().when(goalService).delete(eq(1L), any(User.class));

            mockMvc.perform(delete("/api/v1/goals/1")
                            .with(user(user)))
                    .andExpect(status().isNoContent());

            verify(goalService).delete(eq(1L), any(User.class));
        }
    }
}
