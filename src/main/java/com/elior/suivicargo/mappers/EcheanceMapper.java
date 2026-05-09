package com.elior.suivicargo.mappers;

import com.elior.suivicargo.dtos.EcheanceDto;
import com.elior.suivicargo.models.Echeance;
import org.mapstruct.Mapper;

@Mapper
public interface EcheanceMapper {
    EcheanceDto toDto(Echeance e);
}
