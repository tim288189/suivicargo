package com.elior.suivicargo.mappers;

import com.elior.suivicargo.dtos.UpdateUserRequest;
import com.elior.suivicargo.dtos.UserDto;
import com.elior.suivicargo.models.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper
public interface UserMapper {

    UserDto toDto(User u);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateUserRequest req, @MappingTarget User target);
}
