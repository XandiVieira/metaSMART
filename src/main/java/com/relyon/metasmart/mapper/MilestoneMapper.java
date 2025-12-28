package com.relyon.metasmart.mapper;

import com.relyon.metasmart.entity.progress.Milestone;
import com.relyon.metasmart.entity.progress.dto.MilestoneRequest;
import com.relyon.metasmart.entity.progress.dto.MilestoneResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface MilestoneMapper {

    @Mapping(target = "goalId", source = "goal.id")
    MilestoneResponse toResponse(Milestone milestone);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "goal", ignore = true)
    @Mapping(target = "achieved", ignore = true)
    @Mapping(target = "achievedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Milestone toEntity(MilestoneRequest request);
}
