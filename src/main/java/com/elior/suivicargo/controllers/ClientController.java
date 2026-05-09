package com.elior.suivicargo.controllers;

import com.elior.suivicargo.dtos.ClientDto;
import com.elior.suivicargo.dtos.CreateClientRequest;
import com.elior.suivicargo.dtos.PageResponse;
import com.elior.suivicargo.dtos.UpdateClientRequest;
import com.elior.suivicargo.services.ClientService;
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
@RequestMapping("/v1/clients")
@RequiredArgsConstructor
@Tag(name = "Clients", description = "Gestion des clients (expéditeurs/destinataires)")
public class ClientController {

    private final ClientService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE','SUPERVISOR','ADMIN')")
    @Operation(summary = "Rechercher des clients (par nom, prénom ou téléphone)")
    public PageResponse<ClientDto> search(
            @RequestParam(required = false) String q,
            @ParameterObject Pageable pageable) {
        return PageResponse.of(service.search(q, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','SUPERVISOR','ADMIN')")
    @Operation(summary = "Détail d'un client")
    public ClientDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('EMPLOYEE','SUPERVISOR','ADMIN')")
    @Operation(summary = "Créer un client")
    public ClientDto create(@Valid @RequestBody CreateClientRequest req) {
        return service.create(req);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    @Operation(summary = "Mettre à jour un client")
    public ClientDto update(@PathVariable Long id, @Valid @RequestBody UpdateClientRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    @Operation(summary = "Supprimer (soft delete) un client")
    public void delete(@PathVariable Long id) {
        service.softDelete(id);
    }
}
