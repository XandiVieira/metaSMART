package com.relyon.metasmart.mapper;

import com.relyon.metasmart.entity.guardian.GoalGuardian;
import com.relyon.metasmart.entity.guardian.dto.GoalGuardianResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface GoalGuardianMapper {

    @Mapping(source = "goal.id", target = "goalId")
    @Mapping(source = "goal.title", target = "goalTitle")
    @Mapping(source = "guardian.id", target = "guardianId")
    @Mapping(source = "guardian.name", target = "guardianName")
    @Mapping(source = "guardian.email", target = "guardianEmail")
    @Mapping(source = "owner.id", target = "ownerId")
    @Mapping(source = "owner.name", target = "ownerName")
    @Mapping(source = "createdAt", target = "invitedAt")
    GoalGuardianResponse toResponse(GoalGuardian goalGuardian);

    List<GoalGuardianResponse> toResponseList(List<GoalGuardian> goalGuardians);
}
