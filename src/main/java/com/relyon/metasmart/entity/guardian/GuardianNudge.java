package com.relyon.metasmart.entity.guardian;

import com.relyon.metasmart.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "guardian_nudges")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class GuardianNudge extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_guardian_id", nullable = false)
    private GoalGuardian goalGuardian;

    @Column(nullable = false, length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NudgeType nudgeType;

    private LocalDateTime readAt;

    @Column(length = 10)
    private String reaction;

    public boolean isRead() {
        return readAt != null;
    }
}
