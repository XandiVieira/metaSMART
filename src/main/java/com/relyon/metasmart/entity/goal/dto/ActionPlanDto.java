package com.relyon.metasmart.entity.goal.dto;

import com.relyon.metasmart.entity.actionplan.dto.ActionItemResponse;
import com.relyon.metasmart.entity.actionplan.dto.ScheduledTaskDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionPlanDto {

    private String overview;
    private List<ActionItemResponse> tasks;
    private List<ScheduledTaskDto> scheduledTasks;
}
