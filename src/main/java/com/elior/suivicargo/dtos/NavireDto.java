package com.elior.suivicargo.dtos;

public record NavireDto(
        Long id,
        String nom,
        String imo,
        String pavillon,
        Integer capaciteEvp
) {}
