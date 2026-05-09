package com.elior.suivicargo.dtos;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateNavireRequest(
        @Size(max = 150) String nom,
        @Size(max = 50) String pavillon,
        @Positive Integer capaciteEvp
) {}
