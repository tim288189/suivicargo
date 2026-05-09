package com.elior.suivicargo.dtos;

import com.elior.suivicargo.enums.StatutVoyage;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateVoyageRequest(
        @Size(max = 100) String portDepart,
        @Size(max = 100) String portArrivee,
        LocalDate dateDepart,
        LocalDate etaArrivee,
        LocalDate dateArriveeReelle,
        StatutVoyage statut
) {}
