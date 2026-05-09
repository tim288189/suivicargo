package com.elior.suivicargo.mappers;

import com.elior.suivicargo.dtos.ConteneurDto;
import com.elior.suivicargo.models.Conteneur;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ConteneurMapper {
    @Mapping(target = "voyageId", source = "voyage.id")
    ConteneurDto toDto(Conteneur c);
}
