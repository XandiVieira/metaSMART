package com.relyon.metasmart.mapper;

import com.relyon.metasmart.entity.actionplan.TaskScheduleSlot;
import com.relyon.metasmart.entity.actionplan.dto.ScheduleSlotResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface TaskScheduleSlotMapper {

    @Mapping(target = "actionItemId", source = "actionItem.id")
    @Mapping(target = "rescheduledFromSlotId", source = "rescheduledFromSlot.id")
    @Mapping(target = "active", expression = "java(slot.isActive())")
    ScheduleSlotResponse toResponse(TaskScheduleSlot slot);
}
