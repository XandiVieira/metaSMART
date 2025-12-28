package com.relyon.metasmart.repository;

import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.guardian.GoalGuardian;
import com.relyon.metasmart.entity.guardian.GuardianStatus;
import com.relyon.metasmart.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalGuardianRepository extends JpaRepository<GoalGuardian, Long> {

    // Find guardians for a specific goal
    List<GoalGuardian> findByGoalAndStatusNot(Goal goal, GuardianStatus status);

    List<GoalGuardian> findByGoal(Goal goal);

    // Find by goal and guardian
    Optional<GoalGuardian> findByGoalAndGuardian(Goal goal, User guardian);

    // Check if user is already a guardian for a goal
    boolean existsByGoalAndGuardianAndStatusNot(Goal goal, User guardian, GuardianStatus status);

    // Find pending invitations for a user (as guardian)
    Page<GoalGuardian> findByGuardianAndStatus(User guardian, GuardianStatus status, Pageable pageable);

    // Find all goals a user is guarding (active)
    @Query("SELECT gg FROM GoalGuardian gg WHERE gg.guardian = :guardian AND gg.status = :status")
    Page<GoalGuardian> findActiveGuardianships(@Param("guardian") User guardian, @Param("status") GuardianStatus status, Pageable pageable);

    // Find specific guardianship by goal ID and guardian
    @Query("SELECT gg FROM GoalGuardian gg WHERE gg.goal.id = :goalId AND gg.guardian = :guardian AND gg.status = :status")
    Optional<GoalGuardian> findActiveGuardianship(@Param("goalId") Long goalId, @Param("guardian") User guardian, @Param("status") GuardianStatus status);

    // Count active guardians for a goal
    long countByGoalAndStatus(Goal goal, GuardianStatus status);
}
