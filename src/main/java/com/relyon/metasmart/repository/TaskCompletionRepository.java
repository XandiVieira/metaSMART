package com.relyon.metasmart.repository;

import com.relyon.metasmart.entity.actionplan.ActionItem;
import com.relyon.metasmart.entity.actionplan.TaskCompletion;
import com.relyon.metasmart.entity.goal.Goal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskCompletionRepository extends JpaRepository<TaskCompletion, Long> {

    List<TaskCompletion> findByActionItemOrderByCompletedAtDesc(ActionItem actionItem);

    Page<TaskCompletion> findByActionItemOrderByCompletedAtDesc(ActionItem actionItem, Pageable pageable);

    List<TaskCompletion> findByActionItemAndScheduledDateBetween(ActionItem actionItem, LocalDate startDate, LocalDate endDate);

    long countByActionItem(ActionItem actionItem);

    long countByActionItemAndScheduledDateBetween(ActionItem actionItem, LocalDate startDate, LocalDate endDate);

    void deleteByActionItem(ActionItem actionItem);

    @Modifying
    @Query("DELETE FROM TaskCompletion tc WHERE tc.actionItem.goal = :goal")
    void deleteByGoal(@Param("goal") Goal goal);
}
