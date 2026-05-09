package com.elior.suivicargo.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateConteneurRequest(
        @NotBlank
        @Pattern(regexp = "^[A-Z]{4}\\d{7}$",
                message = "Le numéro de conteneur doit suivre l'ISO 6346 (4 lettres majuscules + 7 chiffres)")
        String numero,

        @NotBlank @Size(max = 20) String typeConteneur,

        Long voyageId
) {}
