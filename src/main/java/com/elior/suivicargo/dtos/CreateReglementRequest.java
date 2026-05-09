package com.elior.suivicargo.dtos;

import com.elior.suivicargo.enums.ModePaiement;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Saisi par l'employé à l'enlèvement (et plus largement pour tout encaissement).
 * <p>L'employé indique simplement le montant reçu et le mode. Le service rattache
 * automatiquement le règlement à la prochaine échéance non payée.
 */
public record CreateReglementRequest(
        @NotNull Long cargaisonId,
        @NotNull @Positive BigDecimal montant,
        @NotNull ModePaiement modePaiement,
        @Size(max = 100) String referenceTransaction,
        @NotNull LocalDate dateReglement,
        Long echeanceId,        // optionnel : si l'utilisateur veut cibler une échéance
        @Size(max = 500) String commentaire
) {}
