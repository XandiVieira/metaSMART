package com.relyon.metasmart.mapper;

import com.relyon.metasmart.entity.guardian.GuardianNudge;
import com.relyon.metasmart.entity.guardian.dto.NudgeResponse;
import com.relyon.metasmart.entity.guardian.dto.SendNudgeRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface GuardianNudgeMapper {

    @Mapping(source = "goalGuardian.id", target = "goalGuardianId")
    @Mapping(source = "goalGuardian.guardian.name", target = "guardianName")
    @Mapping(source = "goalGuardian.goal.title", target = "goalTitle")
    @Mapping(source = "createdAt", target = "sentAt")
    @Mapping(target = "isRead", expression = "java(nudge.isRead())")
    NudgeResponse toResponse(GuardianNudge nudge);

    List<NudgeResponse> toResponseList(List<GuardianNudge> nudges);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "goalGuardian", ignore = true)
    @Mapping(target = "readAt", ignore = true)
    @Mapping(target = "reaction", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    GuardianNudge toEntity(SendNudgeRequest request);
}
