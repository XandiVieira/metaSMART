package com.relyon.metasmart.mapper;

import com.relyon.metasmart.entity.streak.StreakInfo;
import com.relyon.metasmart.entity.streak.dto.StreakResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface StreakMapper {

    @Mapping(target = "goalId", source = "goal.id")
    @Mapping(target = "actionItemId", source = "actionItem.id")
    @Mapping(target = "level", expression = "java(determineLevel(streakInfo))")
    StreakResponse toResponse(StreakInfo streakInfo);

    default String determineLevel(StreakInfo streakInfo) {
        if (streakInfo.isUserLevel()) {
            return "USER";
        } else if (streakInfo.isGoalLevel()) {
            return "GOAL";
        } else {
            return "TASK";
        }
    }
}
