package com.elior.suivicargo.controllers;

import com.elior.suivicargo.dtos.CargaisonDetailDto;
import com.elior.suivicargo.dtos.CargaisonDto;
import com.elior.suivicargo.dtos.CreateCargaisonRequest;
import com.elior.suivicargo.dtos.PageResponse;
import com.elior.suivicargo.enums.StatutCargaison;
import com.elior.suivicargo.services.CargaisonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/cargaisons")
@RequiredArgsConstructor
@Tag(name = "Cargaisons", description = "Gestion des cargaisons (enlèvement, suivi, statut)")
public class CargaisonController {

    private final CargaisonService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE','SUPERVISOR','ADMIN')")
    @Operation(summary = "Lister les cargaisons en cours")
    public PageResponse<CargaisonDto> listEnCours(@ParameterObject Pageable pageable) {
        return PageResponse.of(service.listEnCours(pageable));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    @Operation(summary = "Lister toutes les cargaisons (livrées + en cours)")
    public PageResponse<CargaisonDto> listAll(@ParameterObject Pageable pageable) {
        return PageResponse.of(service.listAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','SUPERVISOR','ADMIN')")
    @Operation(summary = "Détail complet d'une cargaison (infos + historique de statut)")
    public CargaisonDetailDto getById(@PathVariable Long id) {
        return service.getDetailById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('EMPLOYEE','SUPERVISOR','ADMIN')")
    @Operation(summary = "Créer une cargaison à l'enlèvement (numéro de traçage généré automatiquement)")
    public CargaisonDto create(@Valid @RequestBody CreateCargaisonRequest req) {
        return service.create(req);
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    @Operation(summary = "Changer le statut d'une cargaison")
    public CargaisonDto changerStatut(
            @PathVariable Long id,
            @RequestParam StatutCargaison statut,
            @RequestParam(required = false) String commentaire) {
        return service.changerStatut(id, statut, commentaire);
    }

    @PatchMapping("/{id}/voyage")
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    @Operation(summary = "Affecter / détacher une cargaison à un voyage (SUPERVISOR/ADMIN)")
    public CargaisonDto assignerVoyage(
            @PathVariable Long id,
            @RequestParam(required = false) Long voyageId) {
        return service.assignerVoyage(id, voyageId);
    }
}
