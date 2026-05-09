package com.elior.suivicargo.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreatePlanPaiementRequest(
        @NotNull Long cargaisonId,
        @Size(min = 3, max = 3) String devise,
        @NotEmpty @Valid List<CreateEcheanceRequest> echeances
) {}
