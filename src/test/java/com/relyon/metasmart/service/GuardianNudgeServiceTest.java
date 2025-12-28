package com.relyon.metasmart.service;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.goal.GoalStatus;
import com.relyon.metasmart.entity.guardian.GoalGuardian;
import com.relyon.metasmart.entity.guardian.GuardianNudge;
import com.relyon.metasmart.entity.guardian.GuardianPermission;
import com.relyon.metasmart.entity.guardian.GuardianStatus;
import com.relyon.metasmart.entity.guardian.NudgeType;
import com.relyon.metasmart.entity.guardian.dto.NudgeResponse;
import com.relyon.metasmart.entity.guardian.dto.ReactToNudgeRequest;
import com.relyon.metasmart.entity.guardian.dto.SendNudgeRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.AccessDeniedException;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.mapper.GuardianNudgeMapper;
import com.relyon.metasmart.repository.GoalGuardianRepository;
import com.relyon.metasmart.repository.GoalRepository;
import com.relyon.metasmart.repository.GuardianNudgeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuardianNudgeServiceTest {

    @Mock
    private GuardianNudgeRepository guardianNudgeRepository;

    @Mock
    private GoalGuardianRepository goalGuardianRepository;

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private GuardianNudgeMapper guardianNudgeMapper;

    @InjectMocks
    private GuardianNudgeService guardianNudgeService;

    private User owner;
    private User guardian;
    private Goal goal;
    private GoalGuardian goalGuardian;
    private GuardianNudge nudge;
    private SendNudgeRequest sendNudgeRequest;
    private NudgeResponse nudgeResponse;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .name("John Owner")
                .email("john@example.com")
                .build();

        guardian = User.builder()
                .id(2L)
                .name("Jane Guardian")
                .email("jane@example.com")
                .build();

        goal = Goal.builder()
                .id(1L)
                .title("Run 5km")
                .description("Build endurance")
                .goalCategory(GoalCategory.HEALTH)
                .targetValue("5")
                .unit("km")
                .currentProgress(BigDecimal.ZERO)
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusMonths(3))
                .goalStatus(GoalStatus.ACTIVE)
                .owner(owner)
                .build();

        goalGuardian = GoalGuardian.builder()
                .id(1L)
                .goal(goal)
                .guardian(guardian)
                .owner(owner)
                .status(GuardianStatus.ACTIVE)
                .permissions(Set.of(GuardianPermission.VIEW_PROGRESS, GuardianPermission.SEND_NUDGE))
                .build();

        nudge = GuardianNudge.builder()
                .id(1L)
                .goalGuardian(goalGuardian)
                .message("Keep up the great work!")
                .nudgeType(NudgeType.ENCOURAGEMENT)
                .build();

        sendNudgeRequest = SendNudgeRequest.builder()
                .message("Keep up the great work!")
                .nudgeType(NudgeType.ENCOURAGEMENT)
                .build();

        nudgeResponse = NudgeResponse.builder()
                .id(1L)
                .goalGuardianId(1L)
                .guardianName("Jane Guardian")
                .goalTitle("Run 5km")
                .message("Keep up the great work!")
                .nudgeType(NudgeType.ENCOURAGEMENT)
                .isRead(false)
                .build();
    }

    @Nested
    @DisplayName("Send nudge tests")
    class SendNudgeTests {

        @Test
        @DisplayName("Should send nudge successfully")
        void shouldSendNudge() {
            when(goalGuardianRepository.findActiveGuardianship(1L, guardian, GuardianStatus.ACTIVE))
                    .thenReturn(Optional.of(goalGuardian));
            when(guardianNudgeMapper.toEntity(sendNudgeRequest)).thenReturn(nudge);
            when(guardianNudgeRepository.save(any(GuardianNudge.class))).thenReturn(nudge);
            when(guardianNudgeMapper.toResponse(nudge)).thenReturn(nudgeResponse);

            var result = guardianNudgeService.sendNudge(1L, sendNudgeRequest, guardian);

            assertThat(result).isNotNull();
            assertThat(result.getMessage()).isEqualTo("Keep up the great work!");
            verify(guardianNudgeRepository).save(any(GuardianNudge.class));
        }

        @Test
        @DisplayName("Should throw when not an active guardian")
        void shouldThrowWhenNotActiveGuardian() {
            when(goalGuardianRepository.findActiveGuardianship(1L, guardian, GuardianStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> guardianNudgeService.sendNudge(1L, sendNudgeRequest, guardian))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.GUARDIAN_NOT_FOUND);
        }

        @Test
        @DisplayName("Should throw when no nudge permission")
        void shouldThrowWhenNoNudgePermission() {
            goalGuardian.setPermissions(Set.of(GuardianPermission.VIEW_PROGRESS));

            when(goalGuardianRepository.findActiveGuardianship(1L, guardian, GuardianStatus.ACTIVE))
                    .thenReturn(Optional.of(goalGuardian));

            assertThatThrownBy(() -> guardianNudgeService.sendNudge(1L, sendNudgeRequest, guardian))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage(ErrorMessages.GUARDIAN_PERMISSION_DENIED);
        }
    }

    @Nested
    @DisplayName("Get nudges tests")
    class GetNudgesTests {

        @Test
        @DisplayName("Should get nudges for goal")
        void shouldGetNudgesForGoal() {
            Page<GuardianNudge> page = new PageImpl<>(List.of(nudge));
            when(goalRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(goal));
            when(guardianNudgeRepository.findByGoalId(eq(1L), any(Pageable.class))).thenReturn(page);
            when(guardianNudgeMapper.toResponse(any(GuardianNudge.class))).thenReturn(nudgeResponse);

            var result = guardianNudgeService.getNudgesForGoal(1L, owner, Pageable.unpaged());

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getMessage()).isEqualTo("Keep up the great work!");
        }

        @Test
        @DisplayName("Should throw when goal not found")
        void shouldThrowWhenGoalNotFound() {
            when(goalRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> guardianNudgeService.getNudgesForGoal(1L, owner, Pageable.unpaged()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.GOAL_NOT_FOUND);
        }

        @Test
        @DisplayName("Should count unread nudges")
        void shouldCountUnreadNudges() {
            when(guardianNudgeRepository.countUnreadByOwnerId(1L)).thenReturn(5L);

            var result = guardianNudgeService.countUnreadNudges(owner);

            assertThat(result).isEqualTo(5L);
        }

        @Test
        @DisplayName("Should get sent nudges by guardian")
        void shouldGetSentNudges() {
            Page<GuardianNudge> page = new PageImpl<>(List.of(nudge));
            when(guardianNudgeRepository.findSentByGuardian(eq(2L), any(Pageable.class))).thenReturn(page);
            when(guardianNudgeMapper.toResponse(any(GuardianNudge.class))).thenReturn(nudgeResponse);

            var result = guardianNudgeService.getSentNudges(guardian, Pageable.unpaged());

            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Mark as read tests")
    class MarkAsReadTests {

        @Test
        @DisplayName("Should mark nudge as read")
        void shouldMarkNudgeAsRead() {
            when(goalRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(goal));
            when(guardianNudgeRepository.findById(1L)).thenReturn(Optional.of(nudge));
            when(guardianNudgeRepository.save(any(GuardianNudge.class))).thenReturn(nudge);
            when(guardianNudgeMapper.toResponse(any(GuardianNudge.class))).thenReturn(nudgeResponse);

            var result = guardianNudgeService.markAsRead(1L, 1L, owner);

            assertThat(result).isNotNull();
            verify(guardianNudgeRepository).save(argThat(n -> n.getReadAt() != null));
        }

        @Test
        @DisplayName("Should not update readAt if already read")
        void shouldNotUpdateIfAlreadyRead() {
            nudge.setReadAt(LocalDateTime.now().minusHours(1));
            var originalReadAt = nudge.getReadAt();

            when(goalRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(goal));
            when(guardianNudgeRepository.findById(1L)).thenReturn(Optional.of(nudge));
            when(guardianNudgeMapper.toResponse(nudge)).thenReturn(nudgeResponse);

            guardianNudgeService.markAsRead(1L, 1L, owner);

            assertThat(nudge.getReadAt()).isEqualTo(originalReadAt);
            verify(guardianNudgeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw when nudge not found")
        void shouldThrowWhenNudgeNotFound() {
            when(goalRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(goal));
            when(guardianNudgeRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> guardianNudgeService.markAsRead(1L, 1L, owner))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.NUDGE_NOT_FOUND);
        }

        @Test
        @DisplayName("Should throw when nudge belongs to different goal")
        void shouldThrowWhenNudgeBelongsToDifferentGoal() {
            var otherGoal = Goal.builder().id(99L).build();
            goalGuardian.setGoal(otherGoal);

            when(goalRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(goal));
            when(guardianNudgeRepository.findById(1L)).thenReturn(Optional.of(nudge));

            assertThatThrownBy(() -> guardianNudgeService.markAsRead(1L, 1L, owner))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage(ErrorMessages.NUDGE_ACCESS_DENIED);
        }
    }

    @Nested
    @DisplayName("React to nudge tests")
    class ReactToNudgeTests {

        @Test
        @DisplayName("Should react to nudge")
        void shouldReactToNudge() {
            var reactRequest = ReactToNudgeRequest.builder().reaction("THANKS").build();

            when(goalRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(goal));
            when(guardianNudgeRepository.findById(1L)).thenReturn(Optional.of(nudge));
            when(guardianNudgeRepository.save(any(GuardianNudge.class))).thenReturn(nudge);
            when(guardianNudgeMapper.toResponse(any(GuardianNudge.class))).thenReturn(nudgeResponse);

            var result = guardianNudgeService.reactToNudge(1L, 1L, reactRequest, owner);

            assertThat(result).isNotNull();
            verify(guardianNudgeRepository).save(argThat(n ->
                "THANKS".equals(n.getReaction()) && n.getReadAt() != null
            ));
        }
    }
}
