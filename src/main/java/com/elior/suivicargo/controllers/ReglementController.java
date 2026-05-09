package com.elior.suivicargo.controllers;

import com.elior.suivicargo.dtos.CreateReglementRequest;
import com.elior.suivicargo.dtos.PageResponse;
import com.elior.suivicargo.dtos.ReglementDto;
import com.elior.suivicargo.services.ReglementService;
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
@RequestMapping("/v1/reglements")
@RequiredArgsConstructor
@Tag(name = "Règlements", description = "Encaissements (saisis par EMPLOYEE/SUPERVISOR/ADMIN)")
public class ReglementController {

    private final ReglementService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('EMPLOYEE','SUPERVISOR','ADMIN')")
    @Operation(summary = "Enregistrer un règlement reçu (à l'enlèvement ou en cours)")
    public ReglementDto enregistrer(@Valid @RequestBody CreateReglementRequest req) {
        return service.enregistrer(req);
    }

    @GetMapping("/by-cargaison/{cargaisonId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','SUPERVISOR','ADMIN')")
    @Operation(summary = "Lister les règlements d'une cargaison")
    public PageResponse<ReglementDto> listByCargaison(
            @PathVariable Long cargaisonId,
            @ParameterObject Pageable pageable) {
        return PageResponse.of(service.listByCargaison(cargaisonId, pageable));
    }
}
