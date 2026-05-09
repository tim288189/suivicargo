package com.elior.suivicargo.mappers;

import com.elior.suivicargo.dtos.CreateNavireRequest;
import com.elior.suivicargo.dtos.NavireDto;
import com.elior.suivicargo.dtos.UpdateNavireRequest;
import com.elior.suivicargo.models.Navire;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper
public interface NavireMapper {
    NavireDto toDto(Navire n);
    Navire toEntity(CreateNavireRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateNavireRequest req, @MappingTarget Navire target);
}
