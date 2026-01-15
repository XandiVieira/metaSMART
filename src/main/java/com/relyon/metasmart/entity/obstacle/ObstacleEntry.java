package com.relyon.metasmart.entity.obstacle;

import com.relyon.metasmart.entity.AuditableEntity;
import com.relyon.metasmart.entity.goal.Goal;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "obstacle_entries")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ObstacleEntry extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @Column(nullable = false)
    private LocalDate entryDate;

    @Column(nullable = false, length = 1000)
    private String obstacle;

    @Column(length = 1000)
    private String solution;

    @Builder.Default
    @Column(nullable = false)
    private Boolean resolved = false;
}
