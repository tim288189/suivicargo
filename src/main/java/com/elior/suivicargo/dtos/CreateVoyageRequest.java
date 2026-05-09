package com.elior.suivicargo.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateVoyageRequest(
        @NotNull Long navireId,
        @NotBlank @Size(max = 100) String portDepart,
        @NotBlank @Size(max = 100) String portArrivee,
        @NotNull LocalDate dateDepart,
        @NotNull LocalDate etaArrivee
) {}
