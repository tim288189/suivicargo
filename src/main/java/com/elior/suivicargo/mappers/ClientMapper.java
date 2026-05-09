package com.elior.suivicargo.mappers;

import com.elior.suivicargo.dtos.ClientDto;
import com.elior.suivicargo.dtos.CreateClientRequest;
import com.elior.suivicargo.dtos.UpdateClientRequest;
import com.elior.suivicargo.models.Client;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper
public interface ClientMapper {

    ClientDto toDto(Client c);

    Client toEntity(CreateClientRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateClientRequest req, @MappingTarget Client target);
}
