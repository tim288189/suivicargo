package com.elior.suivicargo.controllers;

import com.elior.suivicargo.dtos.ConteneurDto;
import com.elior.suivicargo.dtos.CreateConteneurRequest;
import com.elior.suivicargo.dtos.PageResponse;
import com.elior.suivicargo.dtos.UpdateConteneurRequest;
import com.elior.suivicargo.services.ConteneurService;
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
@RequestMapping("/v1/conteneurs")
@RequiredArgsConstructor
@Tag(name = "Conteneurs", description = "Gestion des conteneurs (réservé SUPERVISOR/ADMIN pour l'écriture)")
public class ConteneurController {

    private final ConteneurService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE','SUPERVISOR','ADMIN')")
    @Operation(summary = "Lister les conteneurs (option : filtrer par voyage)")
    public PageResponse<ConteneurDto> list(
            @RequestParam(required = false) Long voyageId,
            @ParameterObject Pageable pageable) {
        return PageResponse.of(service.list(voyageId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','SUPERVISOR','ADMIN')")
    @Operation(summary = "Détail d'un conteneur")
    public ConteneurDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    @Operation(summary = "Créer un conteneur")
    public ConteneurDto create(@Valid @RequestBody CreateConteneurRequest req) {
        return service.create(req);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    @Operation(summary = "Mettre à jour un conteneur (assignation à un voyage)")
    public ConteneurDto update(@PathVariable Long id, @Valid @RequestBody UpdateConteneurRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    @Operation(summary = "Supprimer (soft delete) un conteneur")
    public void delete(@PathVariable Long id) {
        service.softDelete(id);
    }
}
