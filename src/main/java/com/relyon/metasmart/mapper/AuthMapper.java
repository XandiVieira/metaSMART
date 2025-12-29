package com.relyon.metasmart.mapper;

import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.entity.user.dto.AuthResponse;
import com.relyon.metasmart.entity.user.dto.RegisterRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.lang.NonNull;

@Mapper(config = MapperConfig.class)
public interface AuthMapper {

    @Mapping(target = "token", ignore = true)
    AuthResponse toResponse(@NonNull User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    User toEntity(RegisterRequest request);
}
