package com.relyon.metasmart.entity.feature;

import com.relyon.metasmart.entity.AuditableEntity;
import com.relyon.metasmart.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "feature_preferences")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FeaturePreferences extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder.Default
    @Column(nullable = false)
    private Boolean dailyJournalEnabled = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean streaksEnabled = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean achievementsEnabled = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean analyticsEnabled = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean flightPlanEnabled = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean progressRemindersEnabled = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean milestonesEnabled = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean obstacleTrackingEnabled = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean reflectionsEnabled = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean socialProofEnabled = true;
}
