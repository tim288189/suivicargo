package com.elior.suivicargo.dtos;

import com.elior.suivicargo.enums.Role;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresInMinutes,
        UserInfo user
) {
    public record UserInfo(Long id, String email, String nom, String prenom, Role role) {}
}
