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

    // Active goals (non-archived)
    Page<Goal> findByOwnerAndArchivedAtIsNull(User owner, Pageable pageable);

    Page<Goal> findByOwnerAndGoalStatusAndArchivedAtIsNull(User owner, GoalStatus goalStatus, Pageable pageable);

    Page<Goal> findByOwnerAndGoalCategoryAndArchivedAtIsNull(User owner, GoalCategory goalCategory, Pageable pageable);

    Optional<Goal> findByIdAndOwnerAndArchivedAtIsNull(Long id, User owner);

    List<Goal> findByOwnerAndGoalStatusAndArchivedAtIsNull(User owner, GoalStatus goalStatus);

    // Archived goals
    Page<Goal> findByOwnerAndArchivedAtIsNotNull(User owner, Pageable pageable);

    Optional<Goal> findByIdAndOwnerAndArchivedAtIsNotNull(Long id, User owner);

    // Statistics
    long countByOwnerAndArchivedAtIsNull(User owner);

    long countByOwnerAndGoalStatusAndArchivedAtIsNull(User owner, GoalStatus goalStatus);

    // Search
    @Query("SELECT g FROM Goal g WHERE g.owner = :owner AND g.archivedAt IS NULL " +
            "AND (LOWER(g.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(g.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Goal> searchByOwner(@Param("owner") User owner, @Param("query") String query, Pageable pageable);

    // Combined filters
    @Query("SELECT g FROM Goal g WHERE g.owner = :owner AND g.archivedAt IS NULL " +
            "AND (:status IS NULL OR g.goalStatus = :status) " +
            "AND (:category IS NULL OR g.goalCategory = :category)")
    Page<Goal> findByOwnerWithFilters(
            @Param("owner") User owner,
            @Param("status") GoalStatus status,
            @Param("category") GoalCategory category,
            Pageable pageable);

    // Goals due soon (within days)
    @Query("SELECT g FROM Goal g WHERE g.owner = :owner AND g.archivedAt IS NULL " +
            "AND g.goalStatus = 'ACTIVE' " +
            "AND g.targetDate <= :dueDate ORDER BY g.targetDate ASC")
    List<Goal> findGoalsDueSoon(@Param("owner") User owner, @Param("dueDate") java.time.LocalDate dueDate);

    // Legacy methods (keeping for backward compatibility)
    default Page<Goal> findByOwner(User owner, Pageable pageable) {
        return findByOwnerAndArchivedAtIsNull(owner, pageable);
    }

    default Page<Goal> findByOwnerAndGoalStatus(User owner, GoalStatus goalStatus, Pageable pageable) {
        return findByOwnerAndGoalStatusAndArchivedAtIsNull(owner, goalStatus, pageable);
    }

    default Page<Goal> findByOwnerAndGoalCategory(User owner, GoalCategory goalCategory, Pageable pageable) {
        return findByOwnerAndGoalCategoryAndArchivedAtIsNull(owner, goalCategory, pageable);
    }

    default Optional<Goal> findByIdAndOwner(Long id, User owner) {
        return findByIdAndOwnerAndArchivedAtIsNull(id, owner);
    }

    default List<Goal> findByOwnerAndGoalStatus(User owner, GoalStatus goalStatus) {
        return findByOwnerAndGoalStatusAndArchivedAtIsNull(owner, goalStatus);
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
