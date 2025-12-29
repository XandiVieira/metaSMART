package com.relyon.metasmart.entity.goal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalReminders {

    @Column(name = "reminder_channels", length = 255)
    private String channels;

    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_frequency_interval")
    private FrequencyType frequencyInterval;

    @Column(name = "reminder_custom_minutes")
    private Integer customMinutes;

    @Column(name = "reminder_active")
    private Boolean active;
}
