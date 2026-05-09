package com.elior.suivicargo.dtos;

import com.elior.suivicargo.enums.StatutCargaison;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Vue complète d'une cargaison : infos + historique de statut.
 * Renvoyée par GET /v1/cargaisons/{id}.
 */
public record CargaisonDetailDto(
        Long id,
        String numeroTracage,
        Long clientId,
        String clientNom,
        String clientPrenom,
        String clientTelephone,
        String clientEmail,
        String adresseEnlevement,
        String adresseLivraison,
        Long conteneurId,
        String conteneurNumero,
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
        Instant dateCreation,
        List<EvenementHistoriqueDto> historique
) {}
