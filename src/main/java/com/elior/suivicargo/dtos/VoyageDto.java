package com.elior.suivicargo.dtos;

import com.elior.suivicargo.enums.StatutVoyage;

import java.time.LocalDate;

public record VoyageDto(
        Long id,
        Long navireId,
        String navireNom,
        String portDepart,
        String portArrivee,
        LocalDate dateDepart,
        LocalDate etaArrivee,
        LocalDate dateArriveeReelle,
        StatutVoyage statut
) {}
