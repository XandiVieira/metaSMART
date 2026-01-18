package com.relyon.metasmart.entity.template;

import com.relyon.metasmart.entity.AuditableEntity;
import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "goal_templates")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class GoalTemplate extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 255)
    private String defaultTitle;

    @Column(length = 1000)
    private String defaultDescription;

    @Enumerated(EnumType.STRING)
    private GoalCategory defaultCategory;

    @Column(precision = 19, scale = 2)
    private BigDecimal defaultTargetValue;

    @Column(length = 50)
    private String defaultUnit;

    @Column(length = 500)
    private String defaultMotivation;

    @Builder.Default
    @Column(nullable = false)
    private Integer defaultDurationDays = 90;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isPublic = false;
}
