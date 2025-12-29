package com.relyon.metasmart.mapper;

import com.relyon.metasmart.entity.actionplan.ScheduledTask;
import com.relyon.metasmart.entity.actionplan.dto.ScheduledTaskDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.lang.NonNull;

@Mapper(config = MapperConfig.class)
public interface ScheduledTaskMapper {

    @Mapping(target = "taskId", source = "actionItem.id")
    ScheduledTaskDto toDto(@NonNull ScheduledTask scheduledTask);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "actionItem", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    ScheduledTask toEntity(ScheduledTaskDto dto);
}
