package com.elior.suivicargo.controllers;

import com.elior.suivicargo.dtos.CreateNavireRequest;
import com.elior.suivicargo.dtos.NavireDto;
import com.elior.suivicargo.dtos.PageResponse;
import com.elior.suivicargo.dtos.UpdateNavireRequest;
import com.elior.suivicargo.services.NavireService;
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
@RequestMapping("/v1/navires")
@RequiredArgsConstructor
@Tag(name = "Navires", description = "Gestion des navires (réservé SUPERVISOR/ADMIN)")
public class NavireController {

    private final NavireService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE','SUPERVISOR','ADMIN')")
    @Operation(summary = "Lister les navires (lecture seule pour EMPLOYEE)")
    public PageResponse<NavireDto> list(@ParameterObject Pageable pageable) {
        return PageResponse.of(service.list(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','SUPERVISOR','ADMIN')")
    @Operation(summary = "Détail d'un navire")
    public NavireDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    @Operation(summary = "Créer un navire")
    public NavireDto create(@Valid @RequestBody CreateNavireRequest req) {
        return service.create(req);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    @Operation(summary = "Mettre à jour un navire")
    public NavireDto update(@PathVariable Long id, @Valid @RequestBody UpdateNavireRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    @Operation(summary = "Supprimer (soft delete) un navire")
    public void delete(@PathVariable Long id) {
        service.softDelete(id);
    }
}
