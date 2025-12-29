package com.relyon.metasmart.mapper;

import com.relyon.metasmart.entity.goal.*;
import com.relyon.metasmart.entity.goal.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(config = MapperConfig.class)
public interface GoalMapper {

    @Mapping(target = "smartPillars", ignore = true)
    @Mapping(target = "setupCompletionPercentage", ignore = true)
    @Mapping(target = "currentStreak", source = "streak")
    @Mapping(target = "longestStreak", ignore = true)
    @Mapping(target = "progressPercentage", ignore = true)
    @Mapping(target = "category", source = "goalCategory", qualifiedByName = "toCategoryDto")
    @Mapping(target = "status", source = ".", qualifiedByName = "toStatusDto")
    @Mapping(target = "pillars", source = "pillars", qualifiedByName = "toPillarsDto")
    @Mapping(target = "measurement", source = ".", qualifiedByName = "toMeasurementDto")
    @Mapping(target = "reminders", source = "reminders", qualifiedByName = "toRemindersDto")
    @Mapping(target = "emotionalAnchors", source = "emotionalAnchors", qualifiedByName = "toEmotionalAnchorsDto")
    @Mapping(target = "aiSupport", source = "aiSupport", qualifiedByName = "toAiSupportDto")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "stringToList")
    @Mapping(target = "actionPlan", ignore = true)
    @Mapping(target = "checkins", ignore = true)
    @Mapping(target = "supportSystem", ignore = true)
    @Mapping(target = "milestones", ignore = true)
    GoalResponse toResponse(@NonNull Goal goal);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "currentProgress", ignore = true)
    @Mapping(target = "goalStatus", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(target = "lastStreakShieldUsedAt", ignore = true)
    @Mapping(target = "streak", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "pillars", source = "pillars", qualifiedByName = "toPillarsEntity")
    @Mapping(target = "measurement", source = "measurement", qualifiedByName = "toMeasurementEntity")
    @Mapping(target = "reminders", source = "reminders", qualifiedByName = "toRemindersEntity")
    @Mapping(target = "emotionalAnchors", source = "emotionalAnchors", qualifiedByName = "toEmotionalAnchorsEntity")
    @Mapping(target = "aiSupport", source = "aiSupport", qualifiedByName = "toAiSupportEntity")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "listToString")
    Goal toEntity(GoalRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "currentProgress", ignore = true)
    @Mapping(target = "goalStatus", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(target = "lastStreakShieldUsedAt", ignore = true)
    @Mapping(target = "streak", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "pillars", source = "pillars", qualifiedByName = "toPillarsEntity")
    @Mapping(target = "measurement", source = "measurement", qualifiedByName = "toMeasurementEntity")
    @Mapping(target = "reminders", source = "reminders", qualifiedByName = "toRemindersEntity")
    @Mapping(target = "emotionalAnchors", source = "emotionalAnchors", qualifiedByName = "toEmotionalAnchorsEntity")
    @Mapping(target = "aiSupport", source = "aiSupport", qualifiedByName = "toAiSupportEntity")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "listToString")
    void updateEntity(GoalRequest request, @MappingTarget Goal goal);

    @Named("toCategoryDto")
    default GoalCategoryDto toCategoryDto(GoalCategory category) {
        return GoalCategoryDto.fromCategory(category);
    }

    @Named("toStatusDto")
    default GoalStatusDto toStatusDto(Goal goal) {
        if (goal == null) return null;
        return GoalStatusDto.builder()
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .completedAt(goal.getGoalStatus() == GoalStatus.COMPLETED ? goal.getUpdatedAt() : null)
                .progressPercentage(calculateProgressPercentage(goal))
                .isCompleted(goal.getGoalStatus() == GoalStatus.COMPLETED)
                .build();
    }

    default BigDecimal calculateProgressPercentage(Goal goal) {
        if (goal.getTargetValue() == null || goal.getCurrentProgress() == null) {
            return BigDecimal.ZERO;
        }
        var target = new BigDecimal(goal.getTargetValue());
        if (target.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return goal.getCurrentProgress()
                .multiply(BigDecimal.valueOf(100))
                .divide(target, 2, java.math.RoundingMode.HALF_UP)
                .min(BigDecimal.valueOf(100));
    }

    @Named("toPillarsDto")
    default GoalPillarsDto toPillarsDto(GoalPillars pillars) {
        if (pillars == null) return null;
        return GoalPillarsDto.builder()
                .clarity(pillars.getClarity())
                .metric(pillars.getMetric())
                .actionPlan(pillars.getActionPlan())
                .deadline(pillars.getDeadline())
                .motivation(pillars.getMotivation())
                .build();
    }

    @Named("toPillarsEntity")
    default GoalPillars toPillarsEntity(GoalPillarsDto dto) {
        if (dto == null) return null;
        return GoalPillars.builder()
                .clarity(dto.getClarity())
                .metric(dto.getMetric())
                .actionPlan(dto.getActionPlan())
                .deadline(dto.getDeadline())
                .motivation(dto.getMotivation())
                .build();
    }

    @Named("toMeasurementDto")
    default GoalMeasurementDto toMeasurementDto(Goal goal) {
        var measurement = goal.getMeasurement();
        return GoalMeasurementDto.builder()
                .unit(goal.getUnit())
                .targetValue(goal.getTargetValue() != null ? new BigDecimal(goal.getTargetValue()) : null)
                .currentValue(goal.getCurrentProgress())
                .frequency(measurement != null ? GoalMeasurementDto.FrequencyDto.builder()
                        .type(measurement.getFrequencyType())
                        .value(measurement.getFrequencyValue())
                        .build() : null)
                .build();
    }

    @Named("toMeasurementEntity")
    default GoalMeasurement toMeasurementEntity(GoalMeasurementDto dto) {
        if (dto == null) return null;
        return GoalMeasurement.builder()
                .unit(dto.getUnit())
                .targetValue(dto.getTargetValue())
                .currentValue(dto.getCurrentValue())
                .frequencyType(dto.getFrequency() != null ? dto.getFrequency().getType() : null)
                .frequencyValue(dto.getFrequency() != null ? dto.getFrequency().getValue() : null)
                .build();
    }

    @Named("toRemindersDto")
    default GoalRemindersDto toRemindersDto(GoalReminders reminders) {
        if (reminders == null) return null;
        return GoalRemindersDto.builder()
                .channels(stringToChannelList(reminders.getChannels()))
                .frequency(GoalRemindersDto.FrequencyDto.builder()
                        .interval(reminders.getFrequencyInterval())
                        .customMinutes(reminders.getCustomMinutes())
                        .build())
                .active(reminders.getActive())
                .build();
    }

    @Named("toRemindersEntity")
    default GoalReminders toRemindersEntity(GoalRemindersDto dto) {
        if (dto == null) return null;
        return GoalReminders.builder()
                .channels(channelListToString(dto.getChannels()))
                .frequencyInterval(dto.getFrequency() != null ? dto.getFrequency().getInterval() : null)
                .customMinutes(dto.getFrequency() != null ? dto.getFrequency().getCustomMinutes() : null)
                .active(dto.getActive())
                .build();
    }

    @Named("toEmotionalAnchorsDto")
    default EmotionalAnchorsDto toEmotionalAnchorsDto(EmotionalAnchors anchors) {
        if (anchors == null) return null;
        return EmotionalAnchorsDto.builder()
                .imageUrl(anchors.getImageUrl())
                .audioUrl(anchors.getAudioUrl())
                .customMessage(anchors.getCustomMessage())
                .build();
    }

    @Named("toEmotionalAnchorsEntity")
    default EmotionalAnchors toEmotionalAnchorsEntity(EmotionalAnchorsDto dto) {
        if (dto == null) return null;
        return EmotionalAnchors.builder()
                .imageUrl(dto.getImageUrl())
                .audioUrl(dto.getAudioUrl())
                .customMessage(dto.getCustomMessage())
                .build();
    }

    @Named("toAiSupportDto")
    default AiSupportDto toAiSupportDto(AiSupport aiSupport) {
        if (aiSupport == null) return null;
        return AiSupportDto.builder()
                .suggestedMetric(aiSupport.getSuggestedMetric())
                .suggestedDeadline(aiSupport.getSuggestedDeadline())
                .suggestedActionPlan(aiSupport.getSuggestedActionPlan())
                .build();
    }

    @Named("toAiSupportEntity")
    default AiSupport toAiSupportEntity(AiSupportDto dto) {
        if (dto == null) return null;
        return AiSupport.builder()
                .suggestedMetric(dto.getSuggestedMetric())
                .suggestedDeadline(dto.getSuggestedDeadline())
                .suggestedActionPlan(dto.getSuggestedActionPlan())
                .build();
    }

    @Named("stringToList")
    default List<String> stringToList(String value) {
        if (value == null || value.isBlank()) return Collections.emptyList();
        return Arrays.asList(value.split(","));
    }

    @Named("listToString")
    default String listToString(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        return String.join(",", list);
    }

    default List<ReminderChannel> stringToChannelList(String value) {
        if (value == null || value.isBlank()) return Collections.emptyList();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(ReminderChannel::valueOf)
                .collect(Collectors.toList());
    }

    default String channelListToString(List<ReminderChannel> list) {
        if (list == null || list.isEmpty()) return null;
        return list.stream()
                .map(ReminderChannel::name)
                .collect(Collectors.joining(","));
    }
}
