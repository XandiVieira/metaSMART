package com.relyon.metasmart.entity.goal;

import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "goals")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Goal extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalCategory goalCategory;

    @Column(nullable = false)
    private String targetValue;

    @Column(nullable = false)
    private String unit;

    @Builder.Default
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal currentProgress = BigDecimal.ZERO;

    @Column(length = 500)
    private String motivation;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate targetDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GoalStatus goalStatus = GoalStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    private LocalDate archivedAt;
}
