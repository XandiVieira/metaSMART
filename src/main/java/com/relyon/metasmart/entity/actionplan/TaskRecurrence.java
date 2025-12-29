package com.relyon.metasmart.entity.actionplan;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRecurrence {

    @Column(name = "recurrence_enabled")
    private Boolean enabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_frequency")
    private RecurrenceFrequency frequency;

    @Column(name = "recurrence_interval")
    private Integer interval;

    @Column(name = "recurrence_days_of_week")
    private String daysOfWeek;

    @Column(name = "recurrence_ends_at")
    private LocalDate endsAt;
}
