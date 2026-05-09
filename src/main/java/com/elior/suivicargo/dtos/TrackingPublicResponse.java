package com.elior.suivicargo.dtos;

import com.elior.suivicargo.enums.StatutCargaison;

import java.time.Instant;
import java.util.List;

public record TrackingPublicResponse(
        String numeroTracage,
        StatutCargaison statutActuel,
        Integer nombreColis,
        String portDepart,
        String portArrivee,
        List<EvenementTracking> historique
) {
    public record EvenementTracking(
            StatutCargaison statut,
            Instant dateChangement,
            String commentaire
    ) {}
}
