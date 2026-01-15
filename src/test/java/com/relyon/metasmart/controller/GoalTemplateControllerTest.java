package com.relyon.metasmart.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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
import com.relyon.metasmart.entity.goal.dto.GoalRequest;
import com.relyon.metasmart.entity.template.dto.GoalTemplateRequest;
import com.relyon.metasmart.entity.template.dto.GoalTemplateResponse;
import com.relyon.metasmart.entity.template.dto.UpdateGoalTemplateRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.GlobalExceptionHandler;
import com.relyon.metasmart.service.GoalTemplateService;
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

@WebMvcTest(GoalTemplateController.class)
@Import({SecurityConfig.class, CorsConfig.class, RateLimitConfig.class, GlobalExceptionHandler.class})
class GoalTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @MockitoBean
    private GoalTemplateService goalTemplateService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private User user;
    private GoalTemplateResponse templateResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John")
                .email("john@test.com")
                .password("password")
                .build();

        templateResponse = GoalTemplateResponse.builder()
                .id(1L)
                .name("Running Template")
                .description("Template for running goals")
                .defaultCategory(GoalCategory.HEALTH)
                .defaultDurationDays(90)
                .isPublic(false)
                .build();
    }

    @Nested
    @DisplayName("Create template tests")
    class CreateTests {

        @Test
        @DisplayName("Should create template")
        void shouldCreateTemplate() throws Exception {
            var request = GoalTemplateRequest.builder()
                    .name("Running Template")
                    .description("Template for running goals")
                    .defaultCategory(GoalCategory.HEALTH)
                    .defaultDurationDays(90)
                    .build();

            when(goalTemplateService.create(any(GoalTemplateRequest.class), any(User.class)))
                    .thenReturn(templateResponse);

            mockMvc.perform(post("/api/v1/goal-templates")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Running Template"));
        }
    }

    @Nested
    @DisplayName("Get template tests")
    class GetTests {

        @Test
        @DisplayName("Should get templates by owner")
        void shouldGetTemplatesByOwner() throws Exception {
            var page = new PageImpl<>(List.of(templateResponse));
            when(goalTemplateService.findByOwner(any(User.class), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/goal-templates")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].name").value("Running Template"));
        }

        @Test
        @DisplayName("Should get available templates")
        void shouldGetAvailableTemplates() throws Exception {
            var page = new PageImpl<>(List.of(templateResponse));
            when(goalTemplateService.findAvailable(any(User.class), any(), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/goal-templates/available")
                            .with(user(user)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should get available templates filtered by category")
        void shouldGetAvailableTemplatesFilteredByCategory() throws Exception {
            var page = new PageImpl<>(List.of(templateResponse));
            when(goalTemplateService.findAvailable(any(User.class), eq(GoalCategory.HEALTH), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/goal-templates/available")
                            .param("category", "HEALTH")
                            .with(user(user)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should get public templates")
        void shouldGetPublicTemplates() throws Exception {
            var page = new PageImpl<>(List.of(templateResponse));
            when(goalTemplateService.findPublic(any(), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/goal-templates/public")
                            .with(user(user)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should get public templates filtered by category")
        void shouldGetPublicTemplatesFilteredByCategory() throws Exception {
            var page = new PageImpl<>(List.of(templateResponse));
            when(goalTemplateService.findPublic(eq(GoalCategory.HEALTH), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/goal-templates/public")
                            .param("category", "HEALTH")
                            .with(user(user)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should get template by id")
        void shouldGetTemplateById() throws Exception {
            when(goalTemplateService.findById(eq(1L), any(User.class)))
                    .thenReturn(templateResponse);

            mockMvc.perform(get("/api/v1/goal-templates/1")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Running Template"));
        }

        @Test
        @DisplayName("Should create goal from template")
        void shouldCreateGoalFromTemplate() throws Exception {
            var goalRequest = GoalRequest.builder()
                    .title("Run 5km")
                    .targetValue("5")
                    .unit("km")
                    .startDate(LocalDate.now())
                    .targetDate(LocalDate.now().plusDays(90))
                    .build();

            when(goalTemplateService.createGoalFromTemplate(eq(1L), any(User.class)))
                    .thenReturn(goalRequest);

            mockMvc.perform(get("/api/v1/goal-templates/1/goal")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Run 5km"));
        }
    }

    @Nested
    @DisplayName("Update template tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update template")
        void shouldUpdateTemplate() throws Exception {
            var request = UpdateGoalTemplateRequest.builder()
                    .name("Updated Template")
                    .isPublic(true)
                    .build();

            when(goalTemplateService.update(eq(1L), any(UpdateGoalTemplateRequest.class), any(User.class)))
                    .thenReturn(templateResponse);

            mockMvc.perform(put("/api/v1/goal-templates/1")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Delete template tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete template")
        void shouldDeleteTemplate() throws Exception {
            doNothing().when(goalTemplateService).delete(eq(1L), any(User.class));

            mockMvc.perform(delete("/api/v1/goal-templates/1")
                            .with(user(user)))
                    .andExpect(status().isNoContent());
        }
    }
}
