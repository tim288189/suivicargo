package com.elior.suivicargo.dtos;

import com.elior.suivicargo.enums.StatutCargaison;

import java.time.Instant;

/**
 * Une étape de l'historique de statut d'une cargaison.
 */
public record EvenementHistoriqueDto(
        Long id,
        StatutCargaison ancienStatut,
        StatutCargaison nouveauStatut,
        String commentaire,
        String auteur,
        Instant dateChangement
) {}
