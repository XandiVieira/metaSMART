package com.relyon.metasmart.entity.actionplan;

import com.relyon.metasmart.entity.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "scheduled_tasks")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledTask extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_item_id", nullable = false)
    private ActionItem actionItem;

    @Column(nullable = false)
    private LocalDate scheduledDate;

    @Builder.Default
    @Column(nullable = false)
    private Boolean completed = false;

    @Column
    private LocalDateTime completedAt;
}
