package com.relyon.metasmart.mapper;

import com.relyon.metasmart.entity.journal.DailyJournal;
import com.relyon.metasmart.entity.journal.dto.DailyJournalRequest;
import com.relyon.metasmart.entity.journal.dto.DailyJournalResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface DailyJournalMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "shieldUsed", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    DailyJournal toEntity(DailyJournalRequest request);

    DailyJournalResponse toResponse(DailyJournal entity);
}
