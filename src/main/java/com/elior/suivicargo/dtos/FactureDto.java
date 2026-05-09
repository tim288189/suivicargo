package com.elior.suivicargo.dtos;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record FactureDto(
        Long id,
        String numero,
        Long cargaisonId,
        String numeroTracage,
        LocalDate dateFacture,
        BigDecimal montantHt,
        BigDecimal montantTva,
        BigDecimal montantTtc,
        String devise,
        boolean envoyeeEmail,
        boolean envoyeeWhatsapp,
        Instant dateEnvoi
) {}
