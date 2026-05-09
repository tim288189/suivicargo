package com.elior.suivicargo.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateClientRequest(
        @Size(max = 100) String nom,
        @Size(max = 100) String prenom,

        @Size(max = 30)
        @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Numéro de téléphone invalide")
        String telephone,

        @Email @Size(max = 150) String email,

        @Size(max = 500) String adresseEnlevement,
        @Size(max = 500) String adresseLivraison
) {}
