package com.elior.suivicargo.dtos;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateCargaisonRequest(
        @NotNull Long clientId,

        @NotNull @Min(1)
        Integer nombreColis,

        @PositiveOrZero
        BigDecimal poidsKg,

        @PositiveOrZero
        BigDecimal volumeM3,

        @NotNull @PositiveOrZero
        BigDecimal montantTotal,

        @NotNull @PositiveOrZero
        BigDecimal montantRegle,

        @Size(min = 3, max = 3)
        String devise,

        @Size(max = 1000)
        String observations
) {}
