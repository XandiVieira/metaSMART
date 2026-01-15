package com.relyon.metasmart.mapper;

import com.relyon.metasmart.entity.actionplan.ActionItem;
import com.relyon.metasmart.entity.actionplan.FrequencyGoal;
import com.relyon.metasmart.entity.actionplan.ReminderOverride;
import com.relyon.metasmart.entity.actionplan.TaskRecurrence;
import com.relyon.metasmart.entity.actionplan.dto.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.lang.NonNull;

@Mapper(config = MapperConfig.class)
public interface ActionItemMapper {

    @Mapping(target = "context", source = "context", qualifiedByName = "stringToList")
    @Mapping(target = "dependencies", source = "dependencies", qualifiedByName = "stringToLongList")
    @Mapping(target = "recurrence", source = "recurrence", qualifiedByName = "toRecurrenceDto")
    @Mapping(target = "frequencyGoal", source = "frequencyGoal", qualifiedByName = "toFrequencyGoalDto")
    @Mapping(target = "remindersOverride", source = "reminderOverride", qualifiedByName = "toReminderOverrideDto")
    @Mapping(target = "completionHistory", ignore = true)
    ActionItemResponse toResponse(@NonNull ActionItem actionItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "goal", ignore = true)
    @Mapping(target = "completed", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "targetDate", expression = "java(request.getTargetDate() != null ? request.getTargetDate() : request.getDueDate())")
    @Mapping(target = "context", source = "context", qualifiedByName = "listToString")
    @Mapping(target = "dependencies", source = "dependencies", qualifiedByName = "longListToString")
    @Mapping(target = "recurrence", source = "recurrence", qualifiedByName = "toRecurrenceEntity")
    @Mapping(target = "frequencyGoal", source = "frequencyGoal", qualifiedByName = "toFrequencyGoalEntity")
    @Mapping(target = "reminderOverride", source = "remindersOverride", qualifiedByName = "toReminderOverrideEntity")
    ActionItem toEntity(ActionItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "goal", ignore = true)
    @Mapping(target = "completed", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "targetDate", expression = "java(request.getTargetDate() != null ? request.getTargetDate() : request.getDueDate())")
    @Mapping(target = "context", source = "context", qualifiedByName = "listToString")
    @Mapping(target = "dependencies", source = "dependencies", qualifiedByName = "longListToString")
    @Mapping(target = "recurrence", source = "recurrence", qualifiedByName = "toRecurrenceEntity")
    @Mapping(target = "frequencyGoal", source = "frequencyGoal", qualifiedByName = "toFrequencyGoalEntity")
    @Mapping(target = "reminderOverride", source = "remindersOverride", qualifiedByName = "toReminderOverrideEntity")
    void updateEntity(ActionItemRequest request, @MappingTarget ActionItem actionItem);

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

    @Named("stringToLongList")
    default List<Long> stringToLongList(String value) {
        if (value == null || value.isBlank()) return Collections.emptyList();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    @Named("longListToString")
    default String longListToString(List<Long> list) {
        if (list == null || list.isEmpty()) return null;
        return list.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    @Named("toRecurrenceDto")
    default TaskRecurrenceDto toRecurrenceDto(TaskRecurrence recurrence) {
        if (recurrence == null) return null;
        return TaskRecurrenceDto.builder()
                .enabled(recurrence.getEnabled())
                .frequency(recurrence.getFrequency())
                .interval(recurrence.getInterval())
                .daysOfWeek(stringToIntList(recurrence.getDaysOfWeek()))
                .endsAt(recurrence.getEndsAt())
                .build();
    }

    @Named("toRecurrenceEntity")
    default TaskRecurrence toRecurrenceEntity(TaskRecurrenceDto dto) {
        if (dto == null) return null;
        return TaskRecurrence.builder()
                .enabled(dto.getEnabled())
                .frequency(dto.getFrequency())
                .interval(dto.getInterval())
                .daysOfWeek(intListToString(dto.getDaysOfWeek()))
                .endsAt(dto.getEndsAt())
                .build();
    }

    @Named("toFrequencyGoalDto")
    default FrequencyGoalDto toFrequencyGoalDto(FrequencyGoal goal) {
        if (goal == null) return null;
        return FrequencyGoalDto.builder()
                .count(goal.getCount())
                .period(goal.getPeriod())
                .fixedDays(stringToIntList(goal.getFixedDays()))
                .build();
    }

    @Named("toFrequencyGoalEntity")
    default FrequencyGoal toFrequencyGoalEntity(FrequencyGoalDto dto) {
        if (dto == null) return null;
        return FrequencyGoal.builder()
                .count(dto.getCount())
                .period(dto.getPeriod())
                .fixedDays(intListToString(dto.getFixedDays()))
                .build();
    }

    @Named("toReminderOverrideDto")
    default ReminderOverrideDto toReminderOverrideDto(ReminderOverride override) {
        if (override == null) return null;
        return ReminderOverrideDto.builder()
                .enabled(override.getEnabled())
                .frequency(ReminderOverrideDto.FrequencyDto.builder()
                        .interval(override.getInterval())
                        .customMinutes(override.getCustomMinutes())
                        .build())
                .build();
    }

    @Named("toReminderOverrideEntity")
    default ReminderOverride toReminderOverrideEntity(ReminderOverrideDto dto) {
        if (dto == null) return null;
        return ReminderOverride.builder()
                .enabled(dto.getEnabled())
                .interval(dto.getFrequency() != null ? dto.getFrequency().getInterval() : null)
                .customMinutes(dto.getFrequency() != null ? dto.getFrequency().getCustomMinutes() : null)
                .build();
    }

    default List<Integer> stringToIntList(String value) {
        if (value == null || value.isBlank()) return Collections.emptyList();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    default String intListToString(List<Integer> list) {
        if (list == null || list.isEmpty()) return null;
        return list.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }
}
