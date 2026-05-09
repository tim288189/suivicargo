package com.elior.suivicargo.controllers;

import com.elior.suivicargo.dtos.PageResponse;
import com.elior.suivicargo.dtos.UpdateUserRequest;
import com.elior.suivicargo.dtos.UserDto;
import com.elior.suivicargo.enums.Role;
import com.elior.suivicargo.services.UserService;
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
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Users", description = "Gestion des utilisateurs (réservé ADMIN)")
public class UserController {

    private final UserService service;

    @GetMapping
    @Operation(summary = "Lister les utilisateurs (option : filtrer par rôle)")
    public PageResponse<UserDto> list(
            @RequestParam(required = false) Role role,
            @ParameterObject Pageable pageable) {
        return PageResponse.of(service.list(role, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'un utilisateur")
    public UserDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Mettre à jour un utilisateur")
    public UserDto update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer (soft delete) un utilisateur")
    public void delete(@PathVariable Long id) {
        service.softDelete(id);
    }
}
