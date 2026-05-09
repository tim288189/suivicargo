package com.elior.suivicargo.dtos;

import com.elior.suivicargo.enums.StatutPaiement;

import java.math.BigDecimal;
import java.util.List;

public record PlanPaiementDto(
        Long id,
        Long cargaisonId,
        String numeroTracage,
        BigDecimal montantTotal,
        BigDecimal montantRegle,
        BigDecimal montantRestant,
        String devise,
        StatutPaiement statut,
        List<EcheanceDto> echeances
) {}
