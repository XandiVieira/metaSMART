package com.relyon.metasmart.entity.journal;

import com.relyon.metasmart.entity.AuditableEntity;
import com.relyon.metasmart.entity.user.User;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "daily_journals", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "journal_date"})
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DailyJournal extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "journal_date", nullable = false)
    private LocalDate journalDate;

    @Column(length = 2000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Mood mood;

    @Column(name = "shield_used")
    private Boolean shieldUsed;
}
