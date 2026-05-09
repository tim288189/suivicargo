package com.elior.suivicargo.dtos;

import com.elior.suivicargo.enums.Role;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(max = 100) String nom,
        @Size(max = 100) String prenom,
        @Size(max = 30) String telephone,
        Role role,
        Boolean actif
) {}
