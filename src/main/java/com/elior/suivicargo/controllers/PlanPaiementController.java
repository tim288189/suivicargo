package com.elior.suivicargo.controllers;

import com.elior.suivicargo.dtos.CreatePlanPaiementRequest;
import com.elior.suivicargo.dtos.PlanPaiementDto;
import com.elior.suivicargo.services.PlanPaiementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/plans-paiement")
@RequiredArgsConstructor
@Tag(name = "Plans de paiement", description = "Échéanciers (créés par SUPERVISOR)")
public class PlanPaiementController {

    private final PlanPaiementService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    @Operation(summary = "Créer un plan de paiement avec échéancier (SUPERVISOR/ADMIN)")
    public PlanPaiementDto create(@Valid @RequestBody CreatePlanPaiementRequest req) {
        return service.create(req);
    }

    @GetMapping("/by-cargaison/{cargaisonId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','SUPERVISOR','ADMIN')")
    @Operation(summary = "Récupérer le plan de paiement d'une cargaison")
    public PlanPaiementDto getByCargaisonId(@PathVariable Long cargaisonId) {
        return service.getByCargaisonId(cargaisonId);
    }
}
