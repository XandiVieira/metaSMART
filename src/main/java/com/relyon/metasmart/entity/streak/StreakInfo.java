package com.relyon.metasmart.entity.streak;

import com.relyon.metasmart.entity.AuditableEntity;
import com.relyon.metasmart.entity.actionplan.ActionItem;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "streak_info", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "goal_id", "action_item_id"})
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StreakInfo extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private Goal goal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_item_id")
    private ActionItem actionItem;

    @Builder.Default
    @Column(name = "current_maintained_streak", nullable = false)
    private Integer currentMaintainedStreak = 0;

    @Builder.Default
    @Column(name = "best_maintained_streak", nullable = false)
    private Integer bestMaintainedStreak = 0;

    @Builder.Default
    @Column(name = "current_perfect_streak", nullable = false)
    private Integer currentPerfectStreak = 0;

    @Builder.Default
    @Column(name = "best_perfect_streak", nullable = false)
    private Integer bestPerfectStreak = 0;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    public boolean isUserLevel() {
        return goal == null && actionItem == null;
    }

    public boolean isGoalLevel() {
        return goal != null && actionItem == null;
    }

    public boolean isTaskLevel() {
        return actionItem != null;
    }

    public void incrementMaintainedStreak() {
        this.currentMaintainedStreak++;
        if (this.currentMaintainedStreak > this.bestMaintainedStreak) {
            this.bestMaintainedStreak = this.currentMaintainedStreak;
        }
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void incrementPerfectStreak() {
        this.currentPerfectStreak++;
        if (this.currentPerfectStreak > this.bestPerfectStreak) {
            this.bestPerfectStreak = this.currentPerfectStreak;
        }
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void resetMaintainedStreak() {
        this.currentMaintainedStreak = 0;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void resetPerfectStreak() {
        this.currentPerfectStreak = 0;
        this.lastUpdatedAt = LocalDateTime.now();
    }
}
