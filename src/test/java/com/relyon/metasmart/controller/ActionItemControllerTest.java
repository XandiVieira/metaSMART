package com.relyon.metasmart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.relyon.metasmart.config.SecurityConfig;
import com.relyon.metasmart.entity.actionplan.dto.ActionItemRequest;
import com.relyon.metasmart.entity.actionplan.dto.ActionItemResponse;
import com.relyon.metasmart.entity.actionplan.dto.UpdateActionItemRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.GlobalExceptionHandler;
import com.relyon.metasmart.service.ActionItemService;
import com.relyon.metasmart.config.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ActionItemController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ActionItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @MockitoBean
    private ActionItemService actionItemService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private User user;
    private ActionItemResponse actionItemResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John")
                .email("john@test.com")
                .password("password")
                .build();

        actionItemResponse = ActionItemResponse.builder()
                .id(1L)
                .title("Buy running shoes")
                .description("Get proper running shoes")
                .targetDate(LocalDate.now().plusDays(7))
                .orderIndex(1)
                .completed(false)
                .build();
    }

    @Nested
    @DisplayName("Create action item tests")
    class CreateTests {

        @Test
        @DisplayName("Should create action item")
        void shouldCreateActionItem() throws Exception {
            var request = ActionItemRequest.builder()
                    .title("Buy running shoes")
                    .description("Get proper running shoes")
                    .targetDate(LocalDate.now().plusDays(7))
                    .orderIndex(1)
                    .build();

            when(actionItemService.create(eq(1L), any(ActionItemRequest.class), any(User.class)))
                    .thenReturn(actionItemResponse);

            mockMvc.perform(post("/api/v1/goals/1/action-items")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.title").value("Buy running shoes"));
        }
    }

    @Nested
    @DisplayName("Get action items tests")
    class GetTests {

        @Test
        @DisplayName("Should get action items")
        void shouldGetActionItems() throws Exception {
            when(actionItemService.findByGoal(eq(1L), any(User.class)))
                    .thenReturn(List.of(actionItemResponse));

            mockMvc.perform(get("/api/v1/goals/1/action-items")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title").value("Buy running shoes"));
        }
    }

    @Nested
    @DisplayName("Update action item tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update action item")
        void shouldUpdateActionItem() throws Exception {
            var request = UpdateActionItemRequest.builder()
                    .title("Updated title")
                    .completed(true)
                    .build();

            when(actionItemService.update(eq(1L), eq(1L), any(UpdateActionItemRequest.class), any(User.class)))
                    .thenReturn(actionItemResponse);

            mockMvc.perform(put("/api/v1/goals/1/action-items/1")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Delete action item tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete action item")
        void shouldDeleteActionItem() throws Exception {
            doNothing().when(actionItemService).delete(eq(1L), eq(1L), any(User.class));

            mockMvc.perform(delete("/api/v1/goals/1/action-items/1")
                            .with(user(user)))
                    .andExpect(status().isNoContent());
        }
    }
}
