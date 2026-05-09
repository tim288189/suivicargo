package com.elior.suivicargo.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateNavireRequest(
        @NotBlank @Size(max = 150) String nom,

        @NotBlank
        @Pattern(regexp = "^\\d{7}$", message = "Le numéro IMO doit contenir 7 chiffres")
        String imo,

        @Size(max = 50) String pavillon,

        @Positive Integer capaciteEvp
) {}
