package com.relyon.metasmart.repository;

import com.relyon.metasmart.entity.actionplan.ActionItem;
import com.relyon.metasmart.entity.actionplan.ScheduledTask;
import com.relyon.metasmart.entity.goal.Goal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScheduledTaskRepository extends JpaRepository<ScheduledTask, Long> {

    List<ScheduledTask> findByActionItemOrderByScheduledDateAsc(ActionItem actionItem);

    Page<ScheduledTask> findByActionItemOrderByScheduledDateAsc(ActionItem actionItem, Pageable pageable);

    List<ScheduledTask> findByActionItemAndScheduledDateBetween(ActionItem actionItem, LocalDate startDate, LocalDate endDate);

    List<ScheduledTask> findByActionItemAndCompletedFalseAndScheduledDateLessThanEqual(ActionItem actionItem, LocalDate date);

    Optional<ScheduledTask> findByActionItemAndScheduledDate(ActionItem actionItem, LocalDate date);

    @Query("SELECT st FROM ScheduledTask st WHERE st.actionItem.goal = :goal ORDER BY st.scheduledDate ASC")
    List<ScheduledTask> findByGoalOrderByScheduledDateAsc(@Param("goal") Goal goal);

    @Query("SELECT st FROM ScheduledTask st WHERE st.actionItem.goal = :goal AND st.scheduledDate BETWEEN :startDate AND :endDate ORDER BY st.scheduledDate ASC")
    List<ScheduledTask> findByGoalAndDateRange(@Param("goal") Goal goal, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT st FROM ScheduledTask st WHERE st.actionItem.goal = :goal AND st.completed = false AND st.scheduledDate <= :date ORDER BY st.scheduledDate ASC")
    List<ScheduledTask> findPendingByGoalUntilDate(@Param("goal") Goal goal, @Param("date") LocalDate date);

    void deleteByActionItem(ActionItem actionItem);
}
