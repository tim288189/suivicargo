package com.elior.suivicargo.dtos;

public record ConteneurDto(
        Long id,
        String numero,
        String typeConteneur,
        Long voyageId
) {}
