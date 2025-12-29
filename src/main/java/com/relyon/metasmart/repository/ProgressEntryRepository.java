package com.relyon.metasmart.repository;

import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.progress.ProgressEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProgressEntryRepository extends JpaRepository<ProgressEntry, Long> {

    Page<ProgressEntry> findByGoalOrderByCreatedAtDesc(Goal goal, Pageable pageable);

    Page<ProgressEntry> findByGoalAndCreatedAtBetweenOrderByCreatedAtDesc(
            Goal goal, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Optional<ProgressEntry> findByIdAndGoal(Long id, Goal goal);

    @Query("SELECT COALESCE(SUM(p.progressValue), 0) FROM ProgressEntry p WHERE p.goal = :goal")
    BigDecimal sumValueByGoal(@Param("goal") Goal goal);

    @Query("SELECT DISTINCT CAST(p.createdAt AS LocalDate) FROM ProgressEntry p WHERE p.goal = :goal ORDER BY CAST(p.createdAt AS LocalDate) DESC")
    List<LocalDate> findDistinctProgressDates(@Param("goal") Goal goal);

    void deleteByGoal(Goal goal);

    Optional<ProgressEntry> findTopByGoalOrderByCreatedAtDesc(Goal goal);

    // Social Proof - total progress entries count
    @Query("SELECT COUNT(p) FROM ProgressEntry p")
    long countAllProgressEntries();
}
