package com.elior.suivicargo.dtos;

import com.elior.suivicargo.enums.Role;

import java.time.Instant;

public record UserDto(
        Long id,
        String email,
        String nom,
        String prenom,
        String telephone,
        Role role,
        boolean actif,
        Instant dateCreation
) {}
