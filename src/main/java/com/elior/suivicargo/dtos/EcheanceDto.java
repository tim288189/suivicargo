package com.elior.suivicargo.dtos;

import com.elior.suivicargo.enums.StatutPaiement;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EcheanceDto(
        Long id,
        Integer ordre,
        String libelle,
        BigDecimal montantPrevu,
        LocalDate dateEcheance,
        StatutPaiement statut
) {}
