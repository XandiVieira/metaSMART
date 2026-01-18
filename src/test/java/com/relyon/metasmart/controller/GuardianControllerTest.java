package com.relyon.metasmart.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import com.relyon.metasmart.entity.guardian.GuardianPermission;
import com.relyon.metasmart.entity.guardian.GuardianStatus;
import com.relyon.metasmart.entity.guardian.NudgeType;
import com.relyon.metasmart.entity.guardian.dto.GoalGuardianResponse;
import com.relyon.metasmart.entity.guardian.dto.GuardedGoalResponse;
import com.relyon.metasmart.entity.guardian.dto.NudgeResponse;
import com.relyon.metasmart.entity.guardian.dto.SendNudgeRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.GlobalExceptionHandler;
import com.relyon.metasmart.service.GoalGuardianService;
import com.relyon.metasmart.service.GuardianNudgeService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
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

@WebMvcTest(GuardianController.class)
@Import({SecurityConfig.class, CorsConfig.class, RateLimitConfig.class, GlobalExceptionHandler.class})
class GuardianControllerTest {

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
    private GuardedGoalResponse guardedGoalResponse;
    private NudgeResponse nudgeResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Jane Guardian")
                .email("jane@test.com")
                .password("password")
                .build();

        guardianResponse = GoalGuardianResponse.builder()
                .id(1L)
                .goalId(1L)
                .goalTitle("Run 5km")
                .guardianId(1L)
                .guardianName("Jane Guardian")
                .guardianEmail("jane@test.com")
                .ownerId(2L)
                .ownerName("John Owner")
                .status(GuardianStatus.PENDING)
                .permissions(Set.of(GuardianPermission.VIEW_PROGRESS, GuardianPermission.SEND_NUDGE))
                .invitedAt(LocalDateTime.now())
                .build();

        guardedGoalResponse = GuardedGoalResponse.builder()
                .goalGuardianId(1L)
                .goalId(1L)
                .title("Run 5km")
                .description("Build endurance")
                .category(GoalCategory.HEALTH)
                .status(GoalStatus.ACTIVE)
                .ownerName("John Owner")
                .permissions(Set.of(GuardianPermission.VIEW_PROGRESS))
                .currentProgress(BigDecimal.valueOf(2))
                .targetValue(new BigDecimal("5"))
                .unit("km")
                .progressPercentage(BigDecimal.valueOf(40))
                .build();

        nudgeResponse = NudgeResponse.builder()
                .id(1L)
                .goalGuardianId(1L)
                .guardianName("Jane Guardian")
                .goalTitle("Run 5km")
                .message("Keep going!")
                .nudgeType(NudgeType.ENCOURAGEMENT)
                .isRead(false)
                .sentAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Invitation tests")
    class InvitationTests {

        @Test
        @DisplayName("Should get pending invitations")
        void shouldGetPendingInvitations() throws Exception {
            when(goalGuardianService.getPendingInvitations(any(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(guardianResponse)));

            mockMvc.perform(get("/api/v1/guardian/invitations")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].goalTitle").value("Run 5km"));
        }

        @Test
        @DisplayName("Should accept invitation")
        void shouldAcceptInvitation() throws Exception {
            guardianResponse = GoalGuardianResponse.builder()
                    .id(1L)
                    .status(GuardianStatus.ACTIVE)
                    .build();
            when(goalGuardianService.acceptInvitation(eq(1L), any())).thenReturn(guardianResponse);

            mockMvc.perform(put("/api/v1/guardian/invitations/1/accept")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("Should decline invitation")
        void shouldDeclineInvitation() throws Exception {
            guardianResponse = GoalGuardianResponse.builder()
                    .id(1L)
                    .status(GuardianStatus.DECLINED)
                    .build();
            when(goalGuardianService.declineInvitation(eq(1L), any())).thenReturn(guardianResponse);

            mockMvc.perform(put("/api/v1/guardian/invitations/1/decline")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("DECLINED"));
        }
    }

    @Nested
    @DisplayName("Guarded goals tests")
    class GuardedGoalsTests {

        @Test
        @DisplayName("Should get guarded goals")
        void shouldGetGuardedGoals() throws Exception {
            when(goalGuardianService.getGuardedGoals(any(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(guardianResponse)));

            mockMvc.perform(get("/api/v1/guardian/goals")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].goalTitle").value("Run 5km"));
        }

        @Test
        @DisplayName("Should get guarded goal details")
        void shouldGetGuardedGoalDetails() throws Exception {
            when(goalGuardianService.getGuardedGoalDetails(eq(1L), any())).thenReturn(guardedGoalResponse);

            mockMvc.perform(get("/api/v1/guardian/goals/1")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Run 5km"))
                    .andExpect(jsonPath("$.ownerName").value("John Owner"));
        }
    }

    @Nested
    @DisplayName("Nudge tests")
    class NudgeTests {

        @Test
        @DisplayName("Should send nudge")
        void shouldSendNudge() throws Exception {
            var request = SendNudgeRequest.builder()
                    .message("Keep going!")
                    .nudgeType(NudgeType.ENCOURAGEMENT)
                    .build();

            when(guardianNudgeService.sendNudge(eq(1L), any(), any())).thenReturn(nudgeResponse);

            mockMvc.perform(post("/api/v1/guardian/goals/1/nudges")
                            .with(user(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("Keep going!"));
        }

        @Test
        @DisplayName("Should get sent nudges")
        void shouldGetSentNudges() throws Exception {
            when(guardianNudgeService.getSentNudges(any(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(nudgeResponse)));

            mockMvc.perform(get("/api/v1/guardian/nudges/sent")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].message").value("Keep going!"));
        }

        @Test
        @DisplayName("Should get unread nudges count")
        void shouldGetUnreadNudgesCount() throws Exception {
            when(guardianNudgeService.countUnreadNudges(any())).thenReturn(5L);

            mockMvc.perform(get("/api/v1/guardian/nudges/unread-count")
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("5"));
        }
    }

    @Nested
    @DisplayName("Security tests")
    class SecurityTests {

        @Test
        @DisplayName("Should return 403 when not authenticated")
        void shouldReturn403WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/guardian/goals"))
                    .andExpect(status().isForbidden());
        }
    }
}
