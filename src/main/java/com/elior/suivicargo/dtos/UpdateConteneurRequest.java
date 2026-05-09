package com.elior.suivicargo.dtos;

import jakarta.validation.constraints.Size;

public record UpdateConteneurRequest(
        @Size(max = 20) String typeConteneur,
        Long voyageId
) {}
