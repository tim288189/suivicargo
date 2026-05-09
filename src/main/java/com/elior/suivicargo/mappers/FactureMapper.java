package com.elior.suivicargo.mappers;

import com.elior.suivicargo.dtos.FactureDto;
import com.elior.suivicargo.models.Facture;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface FactureMapper {
    @Mapping(target = "cargaisonId",   source = "cargaison.id")
    @Mapping(target = "numeroTracage", source = "cargaison.numeroTracage")
    FactureDto toDto(Facture f);
}
