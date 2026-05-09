package com.elior.suivicargo.controllers;

import com.elior.suivicargo.dtos.TrackingPublicResponse;
import com.elior.suivicargo.services.CargaisonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tracking PUBLIC : pas d'authentification requise.
 * Le client suit son colis avec son numéro de traçage uniquement.
 */
@RestController
@RequestMapping("/v1/tracking")
@RequiredArgsConstructor
@Tag(name = "Tracking public", description = "Suivi de cargaison sans authentification")
public class TrackingPublicController {

    private final CargaisonService service;

    @GetMapping("/{numeroTracage}")
    @Operation(summary = "Suivre une cargaison par son numéro de traçage")
    public TrackingPublicResponse track(@PathVariable String numeroTracage) {
        return service.trackPublic(numeroTracage);
    }
}
