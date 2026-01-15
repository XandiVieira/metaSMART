package com.relyon.metasmart.repository;

import com.relyon.metasmart.entity.guardian.GoalGuardian;
import com.relyon.metasmart.entity.guardian.GuardianNudge;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GuardianNudgeRepository extends JpaRepository<GuardianNudge, Long> {

    // Find nudges for a specific goal guardian relationship
    Page<GuardianNudge> findByGoalGuardianOrderByCreatedAtDesc(GoalGuardian goalGuardian, Pageable pageable);

    // Find all nudges for a goal (from all guardians)
    @Query("SELECT gn FROM GuardianNudge gn WHERE gn.goalGuardian.goal.id = :goalId ORDER BY gn.createdAt DESC")
    Page<GuardianNudge> findByGoalId(@Param("goalId") Long goalId, Pageable pageable);

    // Find unread nudges for a goal owner
    @Query("SELECT gn FROM GuardianNudge gn WHERE gn.goalGuardian.goal.id = :goalId AND gn.readAt IS NULL ORDER BY gn.createdAt DESC")
    List<GuardianNudge> findUnreadByGoalId(@Param("goalId") Long goalId);

    // Count unread nudges for all goals of a user
    @Query("SELECT COUNT(gn) FROM GuardianNudge gn WHERE gn.goalGuardian.owner.id = :ownerId AND gn.readAt IS NULL")
    long countUnreadByOwnerId(@Param("ownerId") Long ownerId);

    // Find recent nudges sent by a guardian
    @Query("SELECT gn FROM GuardianNudge gn WHERE gn.goalGuardian.guardian.id = :guardianId ORDER BY gn.createdAt DESC")
    Page<GuardianNudge> findSentByGuardian(@Param("guardianId") Long guardianId, Pageable pageable);
}
