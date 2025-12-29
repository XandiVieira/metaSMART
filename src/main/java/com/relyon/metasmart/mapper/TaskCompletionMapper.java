package com.relyon.metasmart.mapper;

import com.relyon.metasmart.entity.actionplan.TaskCompletion;
import com.relyon.metasmart.entity.actionplan.dto.TaskCompletionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.lang.NonNull;

@Mapper(config = MapperConfig.class)
public interface TaskCompletionMapper {

    TaskCompletionDto toDto(@NonNull TaskCompletion completion);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "actionItem", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    TaskCompletion toEntity(TaskCompletionDto dto);
}
