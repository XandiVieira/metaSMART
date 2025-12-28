package com.relyon.metasmart.entity.progress;

import com.relyon.metasmart.entity.AuditableEntity;
import com.relyon.metasmart.entity.goal.Goal;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "milestones")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Milestone extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @Column(nullable = false)
    private Integer percentage;

    @Column(length = 255)
    private String description;

    @Column
    private LocalDateTime achievedAt;

    @Builder.Default
    @Column(nullable = false)
    private Boolean achieved = false;
}
