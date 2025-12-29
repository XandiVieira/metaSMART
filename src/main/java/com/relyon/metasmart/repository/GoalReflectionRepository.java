package com.relyon.metasmart.repository;

import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.reflection.GoalReflection;
import com.relyon.metasmart.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GoalReflectionRepository extends JpaRepository<GoalReflection, Long> {

    Page<GoalReflection> findByGoalAndUserOrderByPeriodEndDesc(Goal goal, User user, Pageable pageable);

    Optional<GoalReflection> findFirstByGoalAndUserOrderByPeriodEndDesc(Goal goal, User user);

    @Query("SELECT gr FROM GoalReflection gr WHERE gr.goal = :goal AND gr.user = :user AND gr.periodStart = :periodStart AND gr.periodEnd = :periodEnd")
    Optional<GoalReflection> findByGoalAndPeriod(@Param("goal") Goal goal, @Param("user") User user,
                                                   @Param("periodStart") LocalDate periodStart,
                                                   @Param("periodEnd") LocalDate periodEnd);

    @Query("SELECT COUNT(gr) FROM GoalReflection gr WHERE gr.goal = :goal AND gr.user = :user")
    int countByGoalAndUser(@Param("goal") Goal goal, @Param("user") User user);

    @Query("SELECT AVG(CASE gr.rating WHEN 'TERRIBLE' THEN 1 WHEN 'POOR' THEN 2 WHEN 'OKAY' THEN 3 WHEN 'GOOD' THEN 4 WHEN 'EXCELLENT' THEN 5 END) FROM GoalReflection gr WHERE gr.goal = :goal AND gr.user = :user")
    Double getAverageRating(@Param("goal") Goal goal, @Param("user") User user);

    List<GoalReflection> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}
