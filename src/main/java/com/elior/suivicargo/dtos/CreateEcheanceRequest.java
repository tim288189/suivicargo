package com.elior.suivicargo.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateEcheanceRequest(
        @NotNull Integer ordre,
        @NotBlank @Size(max = 100) String libelle,
        @NotNull @Positive BigDecimal montantPrevu,
        @NotNull LocalDate dateEcheance
) {}
