package com.elior.suivicargo.dtos;

import java.time.Instant;

public record ClientDto(
        Long id,
        String nom,
        String prenom,
        String telephone,
        String email,
        String adresseEnlevement,
        String adresseLivraison,
        Instant dateCreation
) {}
