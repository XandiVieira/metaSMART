package com.relyon.metasmart.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import com.relyon.metasmart.entity.actionplan.dto.TaskCompletionDto;
import com.relyon.metasmart.entity.actionplan.dto.TaskCompletionRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.GlobalExceptionHandler;
import com.relyon.metasmart.service.TaskCompletionService;
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

@WebMvcTest(TaskCompletionController.class)
@Import({SecurityConfig.class, CorsConfig.class, RateLimitConfig.class, GlobalExceptionHandler.class})
class TaskCompletionControllerTest {

    private static final String BASE_URL = "/api/v1/goals/1/action-items/1/completions";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @MockitoBean
    private TaskCompletionService taskCompletionService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private User user;
    private TaskCompletionDto completionDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John")
                .email("john@test.com")
                .password("password")
                .build();

        completionDto = TaskCompletionDto.builder()
                .id(1L)
                .date(LocalDate.now())
                .completedAt(LocalDateTime.now())
                .note("Completed task successfully")
                .build();
    }

    @Nested
    @DisplayName("Record completion tests")
    class RecordCompletionTests {

        @Test
        @DisplayName("Should record completion with date")
        void shouldRecordCompletionWithDate() throws Exception {
            var request = TaskCompletionRequest.builder()
                    .date(LocalDate.now())
                    .note("Completed task")
                    .build();

            when(taskCompletionService.recordCompletionForDate(
                    eq(1L), eq(1L), any(LocalDate.class), any(String.class), any(User.class)))
                    .thenReturn(completionDto);

            mockMvc.perform(post(BASE_URL)
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.note").value("Completed task successfully"));
        }

        @Test
        @DisplayName("Should record completion without request body")
        void shouldRecordCompletionWithoutRequestBody() throws Exception {
            when(taskCompletionService.recordCompletion(eq(1L), eq(1L), eq(null), any(User.class)))
                    .thenReturn(completionDto);

            mockMvc.perform(post(BASE_URL)
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L));
        }
    }

    @Nested
    @DisplayName("Get completion history tests")
    class GetCompletionHistoryTests {

        @Test
        @DisplayName("Should get completion history")
        void shouldGetCompletionHistory() throws Exception {
            when(taskCompletionService.getCompletionHistory(eq(1L), eq(1L), any(User.class)))
                    .thenReturn(List.of(completionDto));

            mockMvc.perform(get(BASE_URL)
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].note").value("Completed task successfully"));
        }

        @Test
        @DisplayName("Should get paginated completion history")
        void shouldGetPaginatedCompletionHistory() throws Exception {
            var page = new PageImpl<>(List.of(completionDto));
            when(taskCompletionService.getCompletionHistoryPaginated(
                    eq(1L), eq(1L), any(User.class), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get(BASE_URL + "/paginated")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(1L));
        }

        @Test
        @DisplayName("Should get completions by date range")
        void shouldGetCompletionsByDateRange() throws Exception {
            var startDate = LocalDate.now().minusDays(7);
            var endDate = LocalDate.now();

            when(taskCompletionService.getCompletionsByDateRange(
                    eq(1L), eq(1L), any(LocalDate.class), any(LocalDate.class), any(User.class)))
                    .thenReturn(List.of(completionDto));

            mockMvc.perform(get(BASE_URL + "/range")
                            .with(user(user))
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1L));
        }
    }

    @Nested
    @DisplayName("Count completions tests")
    class CountCompletionsTests {

        @Test
        @DisplayName("Should count completions")
        void shouldCountCompletions() throws Exception {
            when(taskCompletionService.countCompletions(eq(1L), eq(1L), any(User.class)))
                    .thenReturn(10L);

            mockMvc.perform(get(BASE_URL + "/count")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(10L));
        }

        @Test
        @DisplayName("Should count completions in period")
        void shouldCountCompletionsInPeriod() throws Exception {
            var startDate = LocalDate.now().minusDays(7);
            var endDate = LocalDate.now();

            when(taskCompletionService.countCompletionsInPeriod(
                    eq(1L), eq(1L), any(LocalDate.class), any(LocalDate.class), any(User.class)))
                    .thenReturn(5L);

            mockMvc.perform(get(BASE_URL + "/count/range")
                            .with(user(user))
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(5L));
        }
    }

    @Nested
    @DisplayName("Delete completion tests")
    class DeleteCompletionTests {

        @Test
        @DisplayName("Should delete completion")
        void shouldDeleteCompletion() throws Exception {
            doNothing().when(taskCompletionService).deleteCompletion(eq(1L), eq(1L), eq(1L), any(User.class));

            mockMvc.perform(delete(BASE_URL + "/1")
                            .with(user(user)))
                    .andExpect(status().isNoContent());
        }
    }
}