package com.elior.suivicargo.mappers;

import com.elior.suivicargo.dtos.PlanPaiementDto;
import com.elior.suivicargo.models.PlanPaiement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(uses = EcheanceMapper.class)
public interface PlanPaiementMapper {

    @Mapping(target = "cargaisonId",     source = "cargaison.id")
    @Mapping(target = "numeroTracage",   source = "cargaison.numeroTracage")
    @Mapping(target = "montantRegle",    expression = "java(montantRegle(p))")
    @Mapping(target = "montantRestant",  expression = "java(montantRestant(p))")
    PlanPaiementDto toDto(PlanPaiement p);

    default BigDecimal montantRegle(PlanPaiement p) {
        return p.getCargaison() != null && p.getCargaison().getMontantRegle() != null
                ? p.getCargaison().getMontantRegle()
                : BigDecimal.ZERO;
    }

    default BigDecimal montantRestant(PlanPaiement p) {
        return p.getCargaison() != null
                ? p.getCargaison().getMontantRestant()
                : p.getMontantTotal();
    }
}
