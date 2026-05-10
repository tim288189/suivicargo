package com.elior.suivicargo.dtos;

import com.elior.suivicargo.enums.StatutCargaison;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record CargaisonDto(
        Long id,
        String numeroTracage,
        Long clientId,
        String clientNomComplet,
        String clientTelephone,
        Long conteneurId,
        String conteneurNumero,
        Long voyageId,
        String voyageNavireNom,
        String voyagePortDepart,
        String voyagePortArrivee,
        Integer nombreColis,
        BigDecimal poidsKg,
        BigDecimal volumeM3,
        BigDecimal montantTotal,
        BigDecimal montantRegle,
        BigDecimal montantRestant,
        String devise,
        StatutCargaison statut,
        String observations,
        boolean factureEnvoyee,
        LocalDate dateEnlevement,
        LocalDate dateLivraisonEstimee,
        LocalDate dateLivraisonReelle,
        Instant dateCreation
) {}
