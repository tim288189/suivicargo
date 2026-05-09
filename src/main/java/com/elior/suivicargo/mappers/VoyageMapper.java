package com.elior.suivicargo.mappers;

import com.elior.suivicargo.dtos.UpdateVoyageRequest;
import com.elior.suivicargo.dtos.VoyageDto;
import com.elior.suivicargo.models.Voyage;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper
public interface VoyageMapper {

    @Mapping(target = "navireId",  source = "navire.id")
    @Mapping(target = "navireNom", source = "navire.nom")
    VoyageDto toDto(Voyage v);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateVoyageRequest req, @MappingTarget Voyage target);
}
