package com.elior.suivicargo.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateClientRequest(
        @NotBlank @Size(max = 100) String nom,
        @NotBlank @Size(max = 100) String prenom,

        @NotBlank @Size(max = 30)
        @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Numéro de téléphone invalide")
        String telephone,

        @Email @Size(max = 150) String email,

        @NotBlank @Size(max = 500) String adresseEnlevement,
        @NotBlank @Size(max = 500) String adresseLivraison
) {}
