package com.relyon.metasmart.entity.progress;

import com.relyon.metasmart.entity.AuditableEntity;
import com.relyon.metasmart.entity.goal.Goal;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "progress_entries")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressEntry extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @Column(name = "progress_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal progressValue;

    @Column(length = 500)
    private String note;
}
