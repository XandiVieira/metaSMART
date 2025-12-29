package com.relyon.metasmart.mapper;

import com.relyon.metasmart.entity.actionplan.ActionItem;
import com.relyon.metasmart.entity.actionplan.dto.ActionItemRequest;
import com.relyon.metasmart.entity.actionplan.dto.ActionItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.lang.NonNull;

@Mapper(config = MapperConfig.class)
public interface ActionItemMapper {

    ActionItemResponse toResponse(@NonNull ActionItem actionItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "goal", ignore = true)
    @Mapping(target = "completed", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    ActionItem toEntity(ActionItemRequest request);
}
