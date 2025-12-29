package com.relyon.metasmart.entity.actionplan;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReminderOverride {

    @Column(name = "reminder_override_enabled")
    private Boolean enabled;

    @Column(name = "reminder_override_interval")
    private String interval;

    @Column(name = "reminder_override_custom_minutes")
    private Integer customMinutes;
}
