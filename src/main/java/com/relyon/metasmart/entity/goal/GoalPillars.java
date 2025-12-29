package com.relyon.metasmart.entity.goal;

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
public class GoalPillars {

    @Column(name = "pillar_clarity", length = 1000)
    private String clarity;

    @Column(name = "pillar_metric", length = 500)
    private String metric;

    @Column(name = "pillar_action_plan", length = 2000)
    private String actionPlan;

    @Column(name = "pillar_deadline", length = 500)
    private String deadline;

    @Column(name = "pillar_motivation", length = 1000)
    private String motivation;
}
