package com.relyon.metasmart.mapper;

import com.relyon.metasmart.entity.progress.ProgressEntry;
import com.relyon.metasmart.entity.progress.dto.ProgressEntryRequest;
import com.relyon.metasmart.entity.progress.dto.ProgressEntryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.lang.NonNull;

@Mapper(config = MapperConfig.class)
public interface ProgressEntryMapper {

    @Mapping(target = "goalId", source = "goal.id")
    ProgressEntryResponse toResponse(@NonNull ProgressEntry progressEntry);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "goal", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    ProgressEntry toEntity(ProgressEntryRequest request);
}
