package com.elior.suivicargo.mappers;

import com.elior.suivicargo.dtos.ReglementDto;
import com.elior.suivicargo.models.Reglement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import com.elior.suivicargo.models.User;

@Mapper
public interface ReglementMapper {

    @Mapping(target = "planPaiementId",     source = "planPaiement.id")
    @Mapping(target = "echeanceId",         source = "echeance.id")
    @Mapping(target = "encaissePaiUserId",  source = "encaissePar.id")
    @Mapping(target = "encaissePar",        source = "encaissePar", qualifiedByName = "userFullName")
    ReglementDto toDto(Reglement r);

    @Named("userFullName")
    default String userFullName(User u) {
        if (u == null) return null;
        return (u.getPrenom() == null ? "" : u.getPrenom()) + " " + (u.getNom() == null ? "" : u.getNom());
    }
}
