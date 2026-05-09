package com.elior.suivicargo.controllers;

import com.elior.suivicargo.dtos.CreateVoyageRequest;
import com.elior.suivicargo.dtos.PageResponse;
import com.elior.suivicargo.dtos.UpdateVoyageRequest;
import com.elior.suivicargo.dtos.VoyageDto;
import com.elior.suivicargo.enums.StatutVoyage;
import com.elior.suivicargo.services.VoyageService;
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
@RequestMapping("/v1/voyages")
@RequiredArgsConstructor
@Tag(name = "Voyages", description = "Gestion des voyages (réservé SUPERVISOR/ADMIN pour l'écriture)")
public class VoyageController {

    private final VoyageService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE','SUPERVISOR','ADMIN')")
    @Operation(summary = "Lister les voyages")
    public PageResponse<VoyageDto> list(
            @RequestParam(required = false) Long navireId,
            @RequestParam(required = false) StatutVoyage statut,
            @ParameterObject Pageable pageable) {
        return PageResponse.of(service.list(navireId, statut, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','SUPERVISOR','ADMIN')")
    @Operation(summary = "Détail d'un voyage")
    public VoyageDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    @Operation(summary = "Créer un voyage")
    public VoyageDto create(@Valid @RequestBody CreateVoyageRequest req) {
        return service.create(req);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    @Operation(summary = "Mettre à jour un voyage (ETA, statut, etc.)")
    public VoyageDto update(@PathVariable Long id, @Valid @RequestBody UpdateVoyageRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    @Operation(summary = "Supprimer (soft delete) un voyage")
    public void delete(@PathVariable Long id) {
        service.softDelete(id);
    }
}
