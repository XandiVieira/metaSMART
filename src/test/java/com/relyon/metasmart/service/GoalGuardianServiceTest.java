package com.relyon.metasmart.service;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.goal.GoalStatus;
import com.relyon.metasmart.entity.guardian.GoalGuardian;
import com.relyon.metasmart.entity.guardian.GuardianPermission;
import com.relyon.metasmart.entity.guardian.GuardianStatus;
import com.relyon.metasmart.entity.guardian.dto.GoalGuardianResponse;
import com.relyon.metasmart.entity.guardian.dto.GuardedGoalResponse;
import com.relyon.metasmart.entity.guardian.dto.InviteGuardianRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.AccessDeniedException;
import com.relyon.metasmart.exception.BadRequestException;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.mapper.GoalGuardianMapper;
import com.relyon.metasmart.repository.ActionItemRepository;
import com.relyon.metasmart.repository.GoalGuardianRepository;
import com.relyon.metasmart.repository.GoalRepository;
import com.relyon.metasmart.repository.ObstacleEntryRepository;
import com.relyon.metasmart.repository.ProgressEntryRepository;
import com.relyon.metasmart.repository.UserRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalGuardianServiceTest {

    @Mock
    private GoalGuardianRepository goalGuardianRepository;

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProgressEntryRepository progressEntryRepository;

    @Mock
    private ActionItemRepository actionItemRepository;

    @Mock
    private ObstacleEntryRepository obstacleEntryRepository;

    @Mock
    private GoalGuardianMapper goalGuardianMapper;

    @InjectMocks
    private GoalGuardianService goalGuardianService;

    private User owner;
    private User guardian;
    private Goal goal;
    private GoalGuardian goalGuardian;
    private InviteGuardianRequest inviteRequest;
    private GoalGuardianResponse guardianResponse;

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
                .status(GuardianStatus.PENDING)
                .permissions(Set.of(GuardianPermission.VIEW_PROGRESS, GuardianPermission.SEND_NUDGE))
                .inviteMessage("Please be my accountability partner!")
                .build();

        inviteRequest = InviteGuardianRequest.builder()
                .guardianEmail("jane@example.com")
                .permissions(Set.of(GuardianPermission.VIEW_PROGRESS, GuardianPermission.SEND_NUDGE))
                .inviteMessage("Please be my accountability partner!")
                .build();

        guardianResponse = GoalGuardianResponse.builder()
                .id(1L)
                .goalId(1L)
                .goalTitle("Run 5km")
                .guardianId(2L)
                .guardianName("Jane Guardian")
                .guardianEmail("jane@example.com")
                .status(GuardianStatus.PENDING)
                .permissions(Set.of(GuardianPermission.VIEW_PROGRESS, GuardianPermission.SEND_NUDGE))
                .build();
    }

    @Nested
    @DisplayName("Invite guardian tests")
    class InviteGuardianTests {

        @Test
        @DisplayName("Should successfully invite a guardian")
        void shouldInviteGuardian() {
            when(goalRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(goal));
            when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(guardian));
            when(goalGuardianRepository.existsByGoalAndGuardianAndStatusNot(goal, guardian, GuardianStatus.REVOKED))
                    .thenReturn(false);
            when(goalGuardianRepository.save(any(GoalGuardian.class))).thenReturn(goalGuardian);
            when(goalGuardianMapper.toResponse(goalGuardian)).thenReturn(guardianResponse);

            var result = goalGuardianService.inviteGuardian(1L, inviteRequest, owner);

            assertThat(result).isNotNull();
            assertThat(result.getGuardianEmail()).isEqualTo("jane@example.com");
            verify(goalGuardianRepository).save(any(GoalGuardian.class));
        }

        @Test
        @DisplayName("Should throw exception when goal not found")
        void shouldThrowWhenGoalNotFound() {
            when(goalRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> goalGuardianService.inviteGuardian(1L, inviteRequest, owner))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.GOAL_NOT_FOUND);
        }

        @Test
        @DisplayName("Should throw exception when guardian user not found")
        void shouldThrowWhenGuardianNotFound() {
            when(goalRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(goal));
            when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> goalGuardianService.inviteGuardian(1L, inviteRequest, owner))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("Should throw exception when user tries to be own guardian")
        void shouldThrowWhenSelfGuardian() {
            var selfInvite = InviteGuardianRequest.builder()
                    .guardianEmail("john@example.com")
                    .permissions(Set.of(GuardianPermission.VIEW_PROGRESS))
                    .build();

            when(goalRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(goal));
            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(owner));

            assertThatThrownBy(() -> goalGuardianService.inviteGuardian(1L, selfInvite, owner))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage(ErrorMessages.CANNOT_BE_OWN_GUARDIAN);
        }

        @Test
        @DisplayName("Should throw exception when guardian already exists")
        void shouldThrowWhenGuardianAlreadyExists() {
            when(goalRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(goal));
            when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(guardian));
            when(goalGuardianRepository.existsByGoalAndGuardianAndStatusNot(goal, guardian, GuardianStatus.REVOKED))
                    .thenReturn(true);

            assertThatThrownBy(() -> goalGuardianService.inviteGuardian(1L, inviteRequest, owner))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage(ErrorMessages.GUARDIAN_ALREADY_EXISTS);
        }
    }

    @Nested
    @DisplayName("Accept invitation tests")
    class AcceptInvitationTests {

        @Test
        @DisplayName("Should successfully accept invitation")
        void shouldAcceptInvitation() {
            when(goalGuardianRepository.findById(1L)).thenReturn(Optional.of(goalGuardian));
            when(goalGuardianRepository.save(any(GoalGuardian.class))).thenReturn(goalGuardian);
            when(goalGuardianMapper.toResponse(any(GoalGuardian.class))).thenReturn(guardianResponse);

            var result = goalGuardianService.acceptInvitation(1L, guardian);

            assertThat(result).isNotNull();
            verify(goalGuardianRepository).save(argThat(gg ->
                gg.getStatus() == GuardianStatus.ACTIVE && gg.getAcceptedAt() != null
            ));
        }

        @Test
        @DisplayName("Should throw exception when invitation not found")
        void shouldThrowWhenInvitationNotFound() {
            when(goalGuardianRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> goalGuardianService.acceptInvitation(1L, guardian))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.GUARDIAN_INVITATION_NOT_FOUND);
        }

        @Test
        @DisplayName("Should throw exception when not the invited guardian")
        void shouldThrowWhenNotInvitedGuardian() {
            var otherUser = User.builder().id(99L).build();
            when(goalGuardianRepository.findById(1L)).thenReturn(Optional.of(goalGuardian));

            assertThatThrownBy(() -> goalGuardianService.acceptInvitation(1L, otherUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage(ErrorMessages.GUARDIAN_PERMISSION_DENIED);
        }

        @Test
        @DisplayName("Should throw exception when invitation not pending")
        void shouldThrowWhenNotPending() {
            goalGuardian.setStatus(GuardianStatus.ACTIVE);
            when(goalGuardianRepository.findById(1L)).thenReturn(Optional.of(goalGuardian));

            assertThatThrownBy(() -> goalGuardianService.acceptInvitation(1L, guardian))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage(ErrorMessages.GUARDIAN_INVITATION_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Decline invitation tests")
    class DeclineInvitationTests {

        @Test
        @DisplayName("Should successfully decline invitation")
        void shouldDeclineInvitation() {
            when(goalGuardianRepository.findById(1L)).thenReturn(Optional.of(goalGuardian));
            when(goalGuardianRepository.save(any(GoalGuardian.class))).thenReturn(goalGuardian);
            when(goalGuardianMapper.toResponse(any(GoalGuardian.class))).thenReturn(guardianResponse);

            var result = goalGuardianService.declineInvitation(1L, guardian);

            assertThat(result).isNotNull();
            verify(goalGuardianRepository).save(argThat(gg ->
                gg.getStatus() == GuardianStatus.DECLINED && gg.getDeclinedAt() != null
            ));
        }
    }

    @Nested
    @DisplayName("Get guardians tests")
    class GetGuardiansTests {

        @Test
        @DisplayName("Should get guardians for goal")
        void shouldGetGuardiansForGoal() {
            when(goalRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(goal));
            when(goalGuardianRepository.findByGoalAndStatusNot(goal, GuardianStatus.REVOKED))
                    .thenReturn(List.of(goalGuardian));
            when(goalGuardianMapper.toResponseList(anyList())).thenReturn(List.of(guardianResponse));

            var result = goalGuardianService.getGuardiansForGoal(1L, owner);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getGuardianEmail()).isEqualTo("jane@example.com");
        }

        @Test
        @DisplayName("Should get pending invitations for guardian")
        void shouldGetPendingInvitations() {
            Page<GoalGuardian> page = new PageImpl<>(List.of(goalGuardian));
            when(goalGuardianRepository.findByGuardianAndStatus(eq(guardian), eq(GuardianStatus.PENDING), any(Pageable.class)))
                    .thenReturn(page);
            when(goalGuardianMapper.toResponse(any(GoalGuardian.class))).thenReturn(guardianResponse);

            var result = goalGuardianService.getPendingInvitations(guardian, Pageable.unpaged());

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should get guarded goals")
        void shouldGetGuardedGoals() {
            goalGuardian.setStatus(GuardianStatus.ACTIVE);
            Page<GoalGuardian> page = new PageImpl<>(List.of(goalGuardian));
            when(goalGuardianRepository.findActiveGuardianships(eq(guardian), eq(GuardianStatus.ACTIVE), any(Pageable.class)))
                    .thenReturn(page);
            when(goalGuardianMapper.toResponse(any(GoalGuardian.class))).thenReturn(guardianResponse);

            var result = goalGuardianService.getGuardedGoals(guardian, Pageable.unpaged());

            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Remove guardian tests")
    class RemoveGuardianTests {

        @Test
        @DisplayName("Should remove guardian successfully")
        void shouldRemoveGuardian() {
            when(goalRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(goal));
            when(goalGuardianRepository.findById(1L)).thenReturn(Optional.of(goalGuardian));
            when(goalGuardianRepository.save(any(GoalGuardian.class))).thenReturn(goalGuardian);

            goalGuardianService.removeGuardian(1L, 1L, owner);

            verify(goalGuardianRepository).save(argThat(gg ->
                gg.getStatus() == GuardianStatus.REVOKED && gg.getRevokedAt() != null
            ));
        }

        @Test
        @DisplayName("Should throw when guardian relationship belongs to different goal")
        void shouldThrowWhenGuardianshipBelongsToDifferentGoal() {
            var otherGoal = Goal.builder().id(99L).build();
            goalGuardian.setGoal(otherGoal);

            when(goalRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(goal));
            when(goalGuardianRepository.findById(1L)).thenReturn(Optional.of(goalGuardian));

            assertThatThrownBy(() -> goalGuardianService.removeGuardian(1L, 1L, owner))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage(ErrorMessages.GUARDIAN_PERMISSION_DENIED);
        }
    }

    @Nested
    @DisplayName("Get guarded goal details tests")
    class GetGuardedGoalDetailsTests {

        @Test
        @DisplayName("Should get guarded goal details with permissions")
        void shouldGetGuardedGoalDetails() {
            goalGuardian.setStatus(GuardianStatus.ACTIVE);
            goalGuardian.setPermissions(Set.of(GuardianPermission.VIEW_PROGRESS, GuardianPermission.VIEW_STREAK));

            when(goalGuardianRepository.findActiveGuardianship(1L, guardian, GuardianStatus.ACTIVE))
                    .thenReturn(Optional.of(goalGuardian));
            when(progressEntryRepository.findTopByGoalOrderByCreatedAtDesc(goal)).thenReturn(Optional.empty());
            when(progressEntryRepository.findDistinctProgressDates(goal)).thenReturn(Collections.emptyList());

            var result = goalGuardianService.getGuardedGoalDetails(1L, guardian);

            assertThat(result).isNotNull();
            assertThat(result.getGoalId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("Run 5km");
            assertThat(result.getOwnerName()).isEqualTo("John Owner");
        }

        @Test
        @DisplayName("Should throw when not an active guardian")
        void shouldThrowWhenNotActiveGuardian() {
            when(goalGuardianRepository.findActiveGuardianship(1L, guardian, GuardianStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> goalGuardianService.getGuardedGoalDetails(1L, guardian))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.GUARDIAN_NOT_FOUND);
        }
    }
}
