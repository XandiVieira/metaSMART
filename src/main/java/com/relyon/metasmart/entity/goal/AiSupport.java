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
public class AiSupport {

    @Column(name = "ai_suggested_metric", length = 500)
    private String suggestedMetric;

    @Column(name = "ai_suggested_deadline", length = 255)
    private String suggestedDeadline;

    @Column(name = "ai_suggested_action_plan", length = 2000)
    private String suggestedActionPlan;
}
