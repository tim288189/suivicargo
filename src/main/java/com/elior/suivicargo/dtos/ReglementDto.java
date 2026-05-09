package com.elior.suivicargo.dtos;

import com.elior.suivicargo.enums.ModePaiement;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReglementDto(
        Long id,
        Long planPaiementId,
        Long echeanceId,
        BigDecimal montant,
        ModePaiement modePaiement,
        String referenceTransaction,
        LocalDate dateReglement,
        Long encaissePaiUserId,
        String encaissePar,
        String commentaire
) {}
