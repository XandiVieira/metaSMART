package com.relyon.metasmart.repository;

import com.relyon.metasmart.entity.actionplan.ActionItem;
import com.relyon.metasmart.entity.actionplan.TaskCompletion;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskCompletionRepository extends JpaRepository<TaskCompletion, Long> {

    List<TaskCompletion> findByActionItemOrderByCompletedAtDesc(ActionItem actionItem);

    Page<TaskCompletion> findByActionItemOrderByCompletedAtDesc(ActionItem actionItem, Pageable pageable);

    List<TaskCompletion> findByActionItemAndDateBetween(ActionItem actionItem, LocalDate startDate, LocalDate endDate);

    long countByActionItem(ActionItem actionItem);

    long countByActionItemAndDateBetween(ActionItem actionItem, LocalDate startDate, LocalDate endDate);

    void deleteByActionItem(ActionItem actionItem);
}
