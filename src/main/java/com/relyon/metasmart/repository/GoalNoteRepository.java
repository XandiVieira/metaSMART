package com.relyon.metasmart.repository;

import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.goal.GoalNote;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalNoteRepository extends JpaRepository<GoalNote, Long> {

    Page<GoalNote> findByGoalOrderByCreatedAtDesc(Goal goal, Pageable pageable);

    Page<GoalNote> findByGoalAndNoteTypeOrderByCreatedAtDesc(Goal goal, GoalNote.NoteType noteType, Pageable pageable);

    Optional<GoalNote> findByIdAndGoal(Long id, Goal goal);

    long countByGoal(Goal goal);
}
