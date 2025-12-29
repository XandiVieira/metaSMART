package com.relyon.metasmart.mapper;

import com.relyon.metasmart.entity.template.GoalTemplate;
import com.relyon.metasmart.entity.template.dto.GoalTemplateRequest;
import com.relyon.metasmart.entity.template.dto.GoalTemplateResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.lang.NonNull;

@Mapper(config = MapperConfig.class)
public interface GoalTemplateMapper {

    GoalTemplateResponse toResponse(@NonNull GoalTemplate template);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    GoalTemplate toEntity(GoalTemplateRequest request);
}
