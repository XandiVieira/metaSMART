package com.relyon.metasmart.mapper;

import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.goal.dto.GoalRequest;
import com.relyon.metasmart.entity.goal.dto.GoalResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.lang.NonNull;

@Mapper(config = MapperConfig.class)
public interface GoalMapper {

    @Mapping(target = "smartPillars", ignore = true)
    @Mapping(target = "setupCompletionPercentage", ignore = true)
    @Mapping(target = "currentStreak", ignore = true)
    @Mapping(target = "longestStreak", ignore = true)
    @Mapping(target = "progressPercentage", ignore = true)
    GoalResponse toResponse(@NonNull Goal goal);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "currentProgress", ignore = true)
    @Mapping(target = "goalStatus", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Goal toEntity(GoalRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "currentProgress", ignore = true)
    @Mapping(target = "goalStatus", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(GoalRequest request, @MappingTarget Goal goal);
}
