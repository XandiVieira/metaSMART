package com.relyon.metasmart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.relyon.metasmart.config.SecurityConfig;
import com.relyon.metasmart.entity.guardian.GuardianPermission;
import com.relyon.metasmart.entity.guardian.GuardianStatus;
import com.relyon.metasmart.entity.guardian.NudgeType;
import com.relyon.metasmart.entity.guardian.dto.GoalGuardianResponse;
import com.relyon.metasmart.entity.guardian.dto.InviteGuardianRequest;
import com.relyon.metasmart.entity.guardian.dto.NudgeResponse;
import com.relyon.metasmart.entity.guardian.dto.ReactToNudgeRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.GlobalExceptionHandler;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.service.GoalGuardianService;
import com.relyon.metasmart.service.GuardianNudgeService;
import com.relyon.metasmart.config.JwtService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GoalGuardianController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class GoalGuardianControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @MockitoBean
    private GoalGuardianService goalGuardianService;

    @MockitoBean
    private GuardianNudgeService guardianNudgeService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private User user;
    private GoalGuardianResponse guardianResponse;
    private NudgeResponse nudgeResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John")
                .email("john@test.com")
                .password("password")
                .build();

        guardianResponse = GoalGuardianResponse.builder()
                .id(1L)
                .goalId(1L)
                .goalTitle("Run 5km")
                .guardianId(2L)
                .guardianName("Jane")
                .guardianEmail("jane@test.com")
                .status(GuardianStatus.PENDING)
                .permissions(Set.of(GuardianPermission.VIEW_PROGRESS, GuardianPermission.SEND_NUDGE))
                .invitedAt(LocalDateTime.now())
                .build();

        nudgeResponse = NudgeResponse.builder()
                .id(1L)
                .goalGuardianId(1L)
                .guardianName("Jane")
                .goalTitle("Run 5km")
                .message("Keep going!")
                .nudgeType(NudgeType.ENCOURAGEMENT)
                .isRead(false)
                .sentAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Invite guardian tests")
    class InviteGuardianTests {

        @Test
        @DisplayName("Should invite guardian successfully")
        void shouldInviteGuardian() throws Exception {
            var request = InviteGuardianRequest.builder()
                    .guardianEmail("jane@test.com")
                    .permissions(Set.of(GuardianPermission.VIEW_PROGRESS))
                    .inviteMessage("Be my partner!")
                    .build();

            when(goalGuardianService.inviteGuardian(eq(1L), any(), any())).thenReturn(guardianResponse);

            mockMvc.perform(post("/api/v1/goals/1/guardians")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.guardianEmail").value("jane@test.com"));
        }

        @Test
        @DisplayName("Should return 403 when not authenticated")
        void shouldReturn403WhenNotAuthenticated() throws Exception {
            mockMvc.perform(post("/api/v1/goals/1/guardians")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Get guardians tests")
    class GetGuardiansTests {

        @Test
        @DisplayName("Should get guardians for goal")
        void shouldGetGuardiansForGoal() throws Exception {
            when(goalGuardianService.getGuardiansForGoal(eq(1L), any())).thenReturn(List.of(guardianResponse));

            mockMvc.perform(get("/api/v1/goals/1/guardians")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].guardianEmail").value("jane@test.com"));
        }
    }

    @Nested
    @DisplayName("Remove guardian tests")
    class RemoveGuardianTests {

        @Test
        @DisplayName("Should remove guardian successfully")
        void shouldRemoveGuardian() throws Exception {
            doNothing().when(goalGuardianService).removeGuardian(eq(1L), eq(1L), any());

            mockMvc.perform(delete("/api/v1/goals/1/guardians/1")
                            .with(user(user)))
                    .andExpect(status().isNoContent());

            verify(goalGuardianService).removeGuardian(eq(1L), eq(1L), any());
        }
    }

    @Nested
    @DisplayName("Nudge endpoints tests")
    class NudgeEndpointsTests {

        @Test
        @DisplayName("Should get nudges for goal")
        void shouldGetNudgesForGoal() throws Exception {
            when(guardianNudgeService.getNudgesForGoal(eq(1L), any(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(nudgeResponse)));

            mockMvc.perform(get("/api/v1/goals/1/guardians/nudges")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].message").value("Keep going!"));
        }

        @Test
        @DisplayName("Should mark nudge as read")
        void shouldMarkNudgeAsRead() throws Exception {
            when(guardianNudgeService.markAsRead(eq(1L), eq(1L), any())).thenReturn(nudgeResponse);

            mockMvc.perform(put("/api/v1/goals/1/guardians/nudges/1/read")
                            .with(user(user)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should react to nudge")
        void shouldReactToNudge() throws Exception {
            var request = ReactToNudgeRequest.builder().reaction("THANKS").build();
            when(guardianNudgeService.reactToNudge(eq(1L), eq(1L), any(), any())).thenReturn(nudgeResponse);

            mockMvc.perform(put("/api/v1/goals/1/guardians/nudges/1/react")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }
}
