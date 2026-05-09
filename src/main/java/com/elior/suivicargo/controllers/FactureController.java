package com.elior.suivicargo.controllers;

import com.elior.suivicargo.dtos.FactureDto;
import com.elior.suivicargo.services.FactureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/factures")
@RequiredArgsConstructor
@Tag(name = "Factures", description = "Consultation et téléchargement des factures")
public class FactureController {

    private final FactureService service;

    @GetMapping("/by-cargaison/{cargaisonId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','SUPERVISOR','ADMIN')")
    @Operation(summary = "Récupérer la facture liée à une cargaison")
    public FactureDto getByCargaisonId(@PathVariable Long cargaisonId) {
        return service.getByCargaisonId(cargaisonId);
    }

    @GetMapping("/by-cargaison/{cargaisonId}/pdf")
    @PreAuthorize("hasAnyRole('EMPLOYEE','SUPERVISOR','ADMIN')")
    @Operation(summary = "Télécharger le PDF de la facture")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long cargaisonId) {
        byte[] pdf = service.genererPdf(cargaisonId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"facture-" + cargaisonId + ".pdf\"")
                .body(pdf);
    }
}
