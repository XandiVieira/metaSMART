package com.relyon.metasmart.repository;

import com.relyon.metasmart.entity.actionplan.ActionItem;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.streak.StreakInfo;
import com.relyon.metasmart.entity.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StreakInfoRepository extends JpaRepository<StreakInfo, Long> {

    Optional<StreakInfo> findByUserAndGoalIsNullAndActionItemIsNull(User user);

    Optional<StreakInfo> findByUserAndGoalAndActionItemIsNull(User user, Goal goal);

    Optional<StreakInfo> findByUserAndActionItem(User user, ActionItem actionItem);

    List<StreakInfo> findByUser(User user);

    @Query("SELECT s FROM StreakInfo s WHERE s.user = :user AND s.goal IS NOT NULL AND s.actionItem IS NULL")
    List<StreakInfo> findGoalStreaksByUser(@Param("user") User user);

    @Query("SELECT s FROM StreakInfo s WHERE s.user = :user AND s.actionItem IS NOT NULL")
    List<StreakInfo> findTaskStreaksByUser(@Param("user") User user);

    List<StreakInfo> findByGoal(Goal goal);

    List<StreakInfo> findByActionItem(ActionItem actionItem);

    void deleteByGoal(Goal goal);

    void deleteByActionItem(ActionItem actionItem);
}
