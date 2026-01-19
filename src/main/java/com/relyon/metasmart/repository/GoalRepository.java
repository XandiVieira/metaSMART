package com.relyon.metasmart.repository;

import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.goal.GoalStatus;
import com.relyon.metasmart.entity.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    // Active goals (non-archived, non-deleted)
    Page<Goal> findByOwnerAndArchivedAtIsNullAndDeletedAtIsNull(User owner, Pageable pageable);

    Page<Goal> findByOwnerAndGoalStatusAndArchivedAtIsNullAndDeletedAtIsNull(User owner, GoalStatus goalStatus, Pageable pageable);

    Page<Goal> findByOwnerAndGoalCategoryAndArchivedAtIsNullAndDeletedAtIsNull(User owner, GoalCategory goalCategory, Pageable pageable);

    Optional<Goal> findByIdAndOwnerAndArchivedAtIsNullAndDeletedAtIsNull(Long id, User owner);

    List<Goal> findByOwnerAndGoalStatusAndArchivedAtIsNullAndDeletedAtIsNull(User owner, GoalStatus goalStatus);

    // Archived goals (non-deleted)
    Page<Goal> findByOwnerAndArchivedAtIsNotNullAndDeletedAtIsNull(User owner, Pageable pageable);

    Optional<Goal> findByIdAndOwnerAndArchivedAtIsNotNullAndDeletedAtIsNull(Long id, User owner);

    // Deleted goals (soft deleted)
    Page<Goal> findByOwnerAndDeletedAtIsNotNull(User owner, Pageable pageable);

    Optional<Goal> findByIdAndOwnerAndDeletedAtIsNotNull(Long id, User owner);

    // Statistics (exclude deleted)
    long countByOwnerAndArchivedAtIsNullAndDeletedAtIsNull(User owner);

    long countByOwnerAndGoalStatusAndArchivedAtIsNullAndDeletedAtIsNull(User owner, GoalStatus goalStatus);

    // Count active (non-locked, non-archived, non-deleted) goals
    @Query("SELECT COUNT(g) FROM Goal g WHERE g.owner = :owner " +
            "AND g.archivedAt IS NULL AND g.deletedAt IS NULL " +
            "AND g.goalStatus NOT IN ('LOCKED', 'COMPLETED', 'ABANDONED')")
    long countActiveGoalsByOwner(@Param("owner") User owner);

    // Find locked goals ordered by createdAt ASC (oldest first for unlocking)
    @Query("SELECT g FROM Goal g WHERE g.owner = :owner " +
            "AND g.goalStatus = 'LOCKED' AND g.deletedAt IS NULL " +
            "ORDER BY g.createdAt ASC")
    List<Goal> findLockedGoalsOrderByCreatedAtAsc(@Param("owner") User owner);

    // Find goals created during premium ordered by createdAt DESC (newest first for locking)
    @Query("SELECT g FROM Goal g WHERE g.owner = :owner " +
            "AND g.createdDuringPremium = true AND g.deletedAt IS NULL " +
            "AND g.archivedAt IS NULL AND g.goalStatus NOT IN ('LOCKED', 'COMPLETED', 'ABANDONED') " +
            "ORDER BY g.createdAt DESC")
    List<Goal> findPremiumGoalsForLocking(@Param("owner") User owner);

    // Search (exclude deleted)
    @Query("SELECT g FROM Goal g WHERE g.owner = :owner AND g.archivedAt IS NULL AND g.deletedAt IS NULL " +
            "AND (LOWER(g.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(g.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Goal> searchByOwner(@Param("owner") User owner, @Param("query") String query, Pageable pageable);

    // Combined filters (exclude deleted)
    @Query("SELECT g FROM Goal g WHERE g.owner = :owner AND g.archivedAt IS NULL AND g.deletedAt IS NULL " +
            "AND (:status IS NULL OR g.goalStatus = :status) " +
            "AND (:category IS NULL OR g.goalCategory = :category)")
    Page<Goal> findByOwnerWithFilters(
            @Param("owner") User owner,
            @Param("status") GoalStatus status,
            @Param("category") GoalCategory category,
            Pageable pageable);

    // Goals due soon (within days, exclude deleted)
    @Query("SELECT g FROM Goal g WHERE g.owner = :owner AND g.archivedAt IS NULL AND g.deletedAt IS NULL " +
            "AND g.goalStatus = 'ACTIVE' " +
            "AND g.targetDate <= :dueDate ORDER BY g.targetDate ASC")
    List<Goal> findGoalsDueSoon(@Param("owner") User owner, @Param("dueDate") java.time.LocalDate dueDate);

    // Legacy methods (keeping for backward compatibility)
    default Page<Goal> findByOwner(User owner, Pageable pageable) {
        return findByOwnerAndArchivedAtIsNullAndDeletedAtIsNull(owner, pageable);
    }

    default Page<Goal> findByOwnerAndGoalStatus(User owner, GoalStatus goalStatus, Pageable pageable) {
        return findByOwnerAndGoalStatusAndArchivedAtIsNullAndDeletedAtIsNull(owner, goalStatus, pageable);
    }

    default Page<Goal> findByOwnerAndGoalCategory(User owner, GoalCategory goalCategory, Pageable pageable) {
        return findByOwnerAndGoalCategoryAndArchivedAtIsNullAndDeletedAtIsNull(owner, goalCategory, pageable);
    }

    default Optional<Goal> findByIdAndOwner(Long id, User owner) {
        return findByIdAndOwnerAndArchivedAtIsNullAndDeletedAtIsNull(id, owner);
    }

    default List<Goal> findByOwnerAndGoalStatus(User owner, GoalStatus goalStatus) {
        return findByOwnerAndGoalStatusAndArchivedAtIsNullAndDeletedAtIsNull(owner, goalStatus);
    }

    default Optional<Goal> findByIdAndOwnerAndArchivedAtIsNotNull(Long id, User owner) {
        return findByIdAndOwnerAndArchivedAtIsNotNullAndDeletedAtIsNull(id, owner);
    }

    default Page<Goal> findByOwnerAndArchivedAtIsNotNull(User owner, Pageable pageable) {
        return findByOwnerAndArchivedAtIsNotNullAndDeletedAtIsNull(owner, pageable);
    }

    default long countByOwnerAndArchivedAtIsNull(User owner) {
        return countByOwnerAndArchivedAtIsNullAndDeletedAtIsNull(owner);
    }

    default long countByOwnerAndGoalStatusAndArchivedAtIsNull(User owner, GoalStatus goalStatus) {
        return countByOwnerAndGoalStatusAndArchivedAtIsNullAndDeletedAtIsNull(owner, goalStatus);
    }

    // Additional legacy methods for DashboardService and GoalNoteService compatibility
    default Page<Goal> findByOwnerAndArchivedAtIsNull(User owner, Pageable pageable) {
        return findByOwnerAndArchivedAtIsNullAndDeletedAtIsNull(owner, pageable);
    }

    default List<Goal> findByOwnerAndGoalStatusAndArchivedAtIsNull(User owner, GoalStatus goalStatus) {
        return findByOwnerAndGoalStatusAndArchivedAtIsNullAndDeletedAtIsNull(owner, goalStatus);
    }

    default Optional<Goal> findByIdAndOwnerAndArchivedAtIsNull(Long id, User owner) {
        return findByIdAndOwnerAndArchivedAtIsNullAndDeletedAtIsNull(id, owner);
    }

    // Social Proof aggregate queries
    @Query("SELECT COUNT(DISTINCT g.owner) FROM Goal g")
    long countDistinctUsers();

    @Query("SELECT COUNT(g) FROM Goal g WHERE g.goalStatus = 'ACTIVE'")
    long countActiveGoals();

    @Query("SELECT COUNT(g) FROM Goal g WHERE g.goalStatus = 'COMPLETED'")
    long countCompletedGoals();

    @Query("SELECT COUNT(g) FROM Goal g WHERE g.goalCategory = :category")
    long countByCategory(@Param("category") GoalCategory category);

    @Query("SELECT COUNT(g) FROM Goal g WHERE g.goalCategory = :category AND g.goalStatus = 'ACTIVE'")
    long countActiveByCatgory(@Param("category") GoalCategory category);

    @Query("SELECT COUNT(g) FROM Goal g WHERE g.goalCategory = :category AND g.goalStatus = 'COMPLETED'")
    long countCompletedByCategory(@Param("category") GoalCategory category);

    @Query("SELECT COUNT(DISTINCT g.owner) FROM Goal g WHERE g.goalCategory = :category")
    long countDistinctUsersByCategory(@Param("category") GoalCategory category);

    @Query("SELECT COUNT(DISTINCT g.owner) FROM Goal g WHERE g.goalCategory = :category AND g.goalStatus IN ('ACTIVE', 'COMPLETED')")
    long countUsersWithSimilarGoals(@Param("category") GoalCategory category);
}
