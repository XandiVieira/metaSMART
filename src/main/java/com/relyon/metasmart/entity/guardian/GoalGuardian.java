package com.relyon.metasmart.entity.guardian;

import com.relyon.metasmart.entity.AuditableEntity;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "goal_guardians", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"goal_id", "guardian_id"})
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class GoalGuardian extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_id", nullable = false)
    private User guardian;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GuardianStatus status = GuardianStatus.PENDING;

    @ElementCollection(targetClass = GuardianPermission.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "guardian_permissions", joinColumns = @JoinColumn(name = "goal_guardian_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permission")
    @Builder.Default
    private Set<GuardianPermission> permissions = new HashSet<>();

    @Column(length = 500)
    private String inviteMessage;

    private LocalDateTime acceptedAt;

    private LocalDateTime declinedAt;

    private LocalDateTime revokedAt;

    public boolean hasPermission(GuardianPermission permission) {
        return permissions.contains(permission);
    }

    public boolean isActive() {
        return status == GuardianStatus.ACTIVE;
    }
}
