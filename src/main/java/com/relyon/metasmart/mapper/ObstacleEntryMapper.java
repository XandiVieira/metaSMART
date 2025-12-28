package com.relyon.metasmart.mapper;

import com.relyon.metasmart.entity.obstacle.ObstacleEntry;
import com.relyon.metasmart.entity.obstacle.dto.ObstacleEntryRequest;
import com.relyon.metasmart.entity.obstacle.dto.ObstacleEntryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ObstacleEntryMapper {

    ObstacleEntryResponse toResponse(ObstacleEntry obstacleEntry);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "goal", ignore = true)
    @Mapping(target = "resolved", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    ObstacleEntry toEntity(ObstacleEntryRequest request);
}
