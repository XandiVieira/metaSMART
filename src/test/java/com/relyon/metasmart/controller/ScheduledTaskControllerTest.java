package com.relyon.metasmart.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.relyon.metasmart.config.CorsConfig;
import com.relyon.metasmart.config.JwtService;
import com.relyon.metasmart.config.RateLimitConfig;
import com.relyon.metasmart.config.SecurityConfig;
import com.relyon.metasmart.entity.actionplan.dto.ScheduledTaskDto;
import com.relyon.metasmart.entity.actionplan.dto.ScheduledTaskRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.GlobalExceptionHandler;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.service.ScheduledTaskService;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ScheduledTaskController.class)
@Import({SecurityConfig.class, CorsConfig.class, RateLimitConfig.class, GlobalExceptionHandler.class})
class ScheduledTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @MockitoBean
    private ScheduledTaskService scheduledTaskService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private User user;
    private ScheduledTaskDto scheduledTaskDto;
    private ScheduledTaskRequest scheduledTaskRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John")
                .email("john@test.com")
                .password("password")
                .build();

        scheduledTaskDto = ScheduledTaskDto.builder()
                .id(1L)
                .taskId(10L)
                .scheduledDate(LocalDate.now().plusDays(1))
                .completed(false)
                .build();

        scheduledTaskRequest = ScheduledTaskRequest.builder()
                .taskId(10L)
                .scheduledDate(LocalDate.now().plusDays(1))
                .build();
    }

    @Nested
    @DisplayName("Create scheduled task tests")
    class CreateScheduledTaskTests {

        @Test
        @DisplayName("Should create scheduled task successfully")
        void shouldCreateScheduledTaskSuccessfully() throws Exception {
            when(scheduledTaskService.createScheduledTask(eq(1L), any(ScheduledTaskRequest.class), any(User.class)))
                    .thenReturn(scheduledTaskDto);

            mockMvc.perform(post("/api/v1/goals/1/scheduled-tasks")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(scheduledTaskRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.taskId").value(10L))
                    .andExpect(jsonPath("$.completed").value(false));

            verify(scheduledTaskService).createScheduledTask(eq(1L), any(ScheduledTaskRequest.class), any(User.class));
        }

        @Test
        @DisplayName("Should return 400 when taskId is missing")
        void shouldReturn400WhenTaskIdMissing() throws Exception {
            var invalidRequest = ScheduledTaskRequest.builder()
                    .scheduledDate(LocalDate.now().plusDays(1))
                    .build();

            mockMvc.perform(post("/api/v1/goals/1/scheduled-tasks")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when scheduledDate is missing")
        void shouldReturn400WhenScheduledDateMissing() throws Exception {
            var invalidRequest = ScheduledTaskRequest.builder()
                    .taskId(10L)
                    .build();

            mockMvc.perform(post("/api/v1/goals/1/scheduled-tasks")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 when goal not found")
        void shouldReturn404WhenGoalNotFound() throws Exception {
            when(scheduledTaskService.createScheduledTask(eq(999L), any(ScheduledTaskRequest.class), any(User.class)))
                    .thenThrow(new ResourceNotFoundException("Goal not found"));

            mockMvc.perform(post("/api/v1/goals/999/scheduled-tasks")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(scheduledTaskRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 when not authenticated")
        void shouldReturn403WhenNotAuthenticated() throws Exception {
            mockMvc.perform(post("/api/v1/goals/1/scheduled-tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(scheduledTaskRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Generate schedule tests")
    class GenerateScheduleTests {

        @Test
        @DisplayName("Should generate schedule successfully")
        void shouldGenerateScheduleSuccessfully() throws Exception {
            var scheduledTasks = List.of(
                    ScheduledTaskDto.builder().id(1L).taskId(10L).scheduledDate(LocalDate.now()).build(),
                    ScheduledTaskDto.builder().id(2L).taskId(10L).scheduledDate(LocalDate.now().plusDays(2)).build(),
                    ScheduledTaskDto.builder().id(3L).taskId(10L).scheduledDate(LocalDate.now().plusDays(4)).build()
            );

            when(scheduledTaskService.generateScheduleForFrequencyTask(
                    eq(1L), eq(10L), any(LocalDate.class), any(LocalDate.class), any(User.class)))
                    .thenReturn(scheduledTasks);

            mockMvc.perform(post("/api/v1/goals/1/scheduled-tasks/generate/10")
                            .with(user(user))
                            .param("startDate", LocalDate.now().toString())
                            .param("endDate", LocalDate.now().plusWeeks(1).toString()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(3));

            verify(scheduledTaskService).generateScheduleForFrequencyTask(
                    eq(1L), eq(10L), any(LocalDate.class), any(LocalDate.class), any(User.class));
        }

        @Test
        @DisplayName("Should return 404 when action item not found")
        void shouldReturn404WhenActionItemNotFound() throws Exception {
            when(scheduledTaskService.generateScheduleForFrequencyTask(
                    eq(1L), eq(999L), any(LocalDate.class), any(LocalDate.class), any(User.class)))
                    .thenThrow(new ResourceNotFoundException("Action item not found"));

            mockMvc.perform(post("/api/v1/goals/1/scheduled-tasks/generate/999")
                            .with(user(user))
                            .param("startDate", LocalDate.now().toString())
                            .param("endDate", LocalDate.now().plusWeeks(1).toString()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Get scheduled tasks tests")
    class GetScheduledTasksTests {

        @Test
        @DisplayName("Should get all scheduled tasks for goal")
        void shouldGetAllScheduledTasksForGoal() throws Exception {
            when(scheduledTaskService.getScheduledTasksByGoal(eq(1L), any(User.class)))
                    .thenReturn(List.of(scheduledTaskDto));

            mockMvc.perform(get("/api/v1/goals/1/scheduled-tasks")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].taskId").value(10L));

            verify(scheduledTaskService).getScheduledTasksByGoal(eq(1L), any(User.class));
        }

        @Test
        @DisplayName("Should get scheduled tasks by date range")
        void shouldGetScheduledTasksByDateRange() throws Exception {
            when(scheduledTaskService.getScheduledTasksByDateRange(
                    eq(1L), any(LocalDate.class), any(LocalDate.class), any(User.class)))
                    .thenReturn(List.of(scheduledTaskDto));

            mockMvc.perform(get("/api/v1/goals/1/scheduled-tasks")
                            .with(user(user))
                            .param("startDate", LocalDate.now().toString())
                            .param("endDate", LocalDate.now().plusWeeks(1).toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1L));

            verify(scheduledTaskService).getScheduledTasksByDateRange(
                    eq(1L), any(LocalDate.class), any(LocalDate.class), any(User.class));
        }

        @Test
        @DisplayName("Should return empty list when no tasks")
        void shouldReturnEmptyListWhenNoTasks() throws Exception {
            when(scheduledTaskService.getScheduledTasksByGoal(eq(1L), any(User.class)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/v1/goals/1/scheduled-tasks")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("Get scheduled tasks by action item tests")
    class GetScheduledTasksByActionItemTests {

        @Test
        @DisplayName("Should get scheduled tasks by action item successfully")
        void shouldGetScheduledTasksByActionItemSuccessfully() throws Exception {
            when(scheduledTaskService.getScheduledTasksByActionItem(eq(1L), eq(10L), any(User.class)))
                    .thenReturn(List.of(scheduledTaskDto));

            mockMvc.perform(get("/api/v1/goals/1/scheduled-tasks/action-item/10")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].taskId").value(10L));

            verify(scheduledTaskService).getScheduledTasksByActionItem(eq(1L), eq(10L), any(User.class));
        }

        @Test
        @DisplayName("Should return empty list when no tasks for action item")
        void shouldReturnEmptyListWhenNoTasksForActionItem() throws Exception {
            when(scheduledTaskService.getScheduledTasksByActionItem(eq(1L), eq(10L), any(User.class)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/v1/goals/1/scheduled-tasks/action-item/10")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("Get pending tasks tests")
    class GetPendingTasksTests {

        @Test
        @DisplayName("Should get pending tasks successfully")
        void shouldGetPendingTasksSuccessfully() throws Exception {
            var overdueTask = ScheduledTaskDto.builder()
                    .id(2L)
                    .taskId(10L)
                    .scheduledDate(LocalDate.now().minusDays(1))
                    .completed(false)
                    .build();

            when(scheduledTaskService.getPendingTasks(eq(1L), any(User.class)))
                    .thenReturn(List.of(overdueTask, scheduledTaskDto));

            mockMvc.perform(get("/api/v1/goals/1/scheduled-tasks/pending")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2));

            verify(scheduledTaskService).getPendingTasks(eq(1L), any(User.class));
        }

        @Test
        @DisplayName("Should return empty list when no pending tasks")
        void shouldReturnEmptyListWhenNoPendingTasks() throws Exception {
            when(scheduledTaskService.getPendingTasks(eq(1L), any(User.class)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/v1/goals/1/scheduled-tasks/pending")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("Mark as completed tests")
    class MarkAsCompletedTests {

        @Test
        @DisplayName("Should mark task as completed successfully")
        void shouldMarkTaskAsCompletedSuccessfully() throws Exception {
            var completedTask = ScheduledTaskDto.builder()
                    .id(1L)
                    .taskId(10L)
                    .scheduledDate(LocalDate.now())
                    .completed(true)
                    .completedAt(LocalDateTime.now())
                    .build();

            when(scheduledTaskService.markAsCompleted(eq(1L), eq(1L), any(User.class)))
                    .thenReturn(completedTask);

            mockMvc.perform(patch("/api/v1/goals/1/scheduled-tasks/1/complete")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.completed").value(true))
                    .andExpect(jsonPath("$.completedAt").exists());

            verify(scheduledTaskService).markAsCompleted(eq(1L), eq(1L), any(User.class));
        }

        @Test
        @DisplayName("Should return 404 when task not found")
        void shouldReturn404WhenTaskNotFound() throws Exception {
            when(scheduledTaskService.markAsCompleted(eq(1L), eq(999L), any(User.class)))
                    .thenThrow(new ResourceNotFoundException("Scheduled task not found"));

            mockMvc.perform(patch("/api/v1/goals/1/scheduled-tasks/999/complete")
                            .with(user(user)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Mark as incomplete tests")
    class MarkAsIncompleteTests {

        @Test
        @DisplayName("Should mark task as incomplete successfully")
        void shouldMarkTaskAsIncompleteSuccessfully() throws Exception {
            var incompleteTask = ScheduledTaskDto.builder()
                    .id(1L)
                    .taskId(10L)
                    .scheduledDate(LocalDate.now())
                    .completed(false)
                    .completedAt(null)
                    .build();

            when(scheduledTaskService.markAsIncomplete(eq(1L), eq(1L), any(User.class)))
                    .thenReturn(incompleteTask);

            mockMvc.perform(patch("/api/v1/goals/1/scheduled-tasks/1/incomplete")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.completed").value(false));

            verify(scheduledTaskService).markAsIncomplete(eq(1L), eq(1L), any(User.class));
        }

        @Test
        @DisplayName("Should return 404 when task not found for incomplete")
        void shouldReturn404WhenTaskNotFoundForIncomplete() throws Exception {
            when(scheduledTaskService.markAsIncomplete(eq(1L), eq(999L), any(User.class)))
                    .thenThrow(new ResourceNotFoundException("Scheduled task not found"));

            mockMvc.perform(patch("/api/v1/goals/1/scheduled-tasks/999/incomplete")
                            .with(user(user)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Delete scheduled task tests")
    class DeleteScheduledTaskTests {

        @Test
        @DisplayName("Should delete scheduled task successfully")
        void shouldDeleteScheduledTaskSuccessfully() throws Exception {
            doNothing().when(scheduledTaskService).deleteScheduledTask(eq(1L), eq(1L), any(User.class));

            mockMvc.perform(delete("/api/v1/goals/1/scheduled-tasks/1")
                            .with(user(user)))
                    .andExpect(status().isNoContent());

            verify(scheduledTaskService).deleteScheduledTask(eq(1L), eq(1L), any(User.class));
        }

        @Test
        @DisplayName("Should return 404 when task not found for delete")
        void shouldReturn404WhenTaskNotFoundForDelete() throws Exception {
            doThrow(new ResourceNotFoundException("Scheduled task not found"))
                    .when(scheduledTaskService).deleteScheduledTask(eq(1L), eq(999L), any(User.class));

            mockMvc.perform(delete("/api/v1/goals/1/scheduled-tasks/999")
                            .with(user(user)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 when not authenticated for delete")
        void shouldReturn403WhenNotAuthenticatedForDelete() throws Exception {
            mockMvc.perform(delete("/api/v1/goals/1/scheduled-tasks/1"))
                    .andExpect(status().isForbidden());
        }
    }
}
