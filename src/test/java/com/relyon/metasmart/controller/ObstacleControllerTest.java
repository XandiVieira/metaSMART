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
import com.relyon.metasmart.entity.obstacle.dto.ObstacleEntryRequest;
import com.relyon.metasmart.entity.obstacle.dto.ObstacleEntryResponse;
import com.relyon.metasmart.entity.obstacle.dto.UpdateObstacleEntryRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.GlobalExceptionHandler;
import com.relyon.metasmart.service.ObstacleService;
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

@WebMvcTest(ObstacleController.class)
@Import({SecurityConfig.class, CorsConfig.class, RateLimitConfig.class, GlobalExceptionHandler.class})
class ObstacleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @MockitoBean
    private ObstacleService obstacleService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private User user;
    private ObstacleEntryResponse obstacleResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John")
                .email("john@test.com")
                .password("password")
                .build();

        obstacleResponse = ObstacleEntryResponse.builder()
                .id(1L)
                .entryDate(LocalDate.now())
                .obstacle("Knee pain")
                .solution("Stretching")
                .resolved(false)
                .build();
    }

    @Nested
    @DisplayName("Create obstacle tests")
    class CreateTests {

        @Test
        @DisplayName("Should create obstacle entry")
        void shouldCreateObstacleEntry() throws Exception {
            var request = ObstacleEntryRequest.builder()
                    .obstacle("Knee pain")
                    .solution("Stretching")
                    .build();

            when(obstacleService.create(eq(1L), any(ObstacleEntryRequest.class), any(User.class)))
                    .thenReturn(obstacleResponse);

            mockMvc.perform(post("/api/v1/goals/1/obstacles")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.obstacle").value("Knee pain"));
        }
    }

    @Nested
    @DisplayName("Get obstacles tests")
    class GetTests {

        @Test
        @DisplayName("Should get obstacles")
        void shouldGetObstacles() throws Exception {
            var page = new PageImpl<>(List.of(obstacleResponse));
            when(obstacleService.findByGoal(eq(1L), any(User.class), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/goals/1/obstacles")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].obstacle").value("Knee pain"));
        }
    }

    @Nested
    @DisplayName("Update obstacle tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update obstacle entry")
        void shouldUpdateObstacleEntry() throws Exception {
            var request = UpdateObstacleEntryRequest.builder()
                    .solution("New solution")
                    .resolved(true)
                    .build();

            when(obstacleService.update(eq(1L), eq(1L), any(UpdateObstacleEntryRequest.class), any(User.class)))
                    .thenReturn(obstacleResponse);

            mockMvc.perform(put("/api/v1/goals/1/obstacles/1")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Delete obstacle tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete obstacle entry")
        void shouldDeleteObstacleEntry() throws Exception {
            doNothing().when(obstacleService).delete(eq(1L), eq(1L), any(User.class));

            mockMvc.perform(delete("/api/v1/goals/1/obstacles/1")
                            .with(user(user)))
                    .andExpect(status().isNoContent());
        }
    }
}
