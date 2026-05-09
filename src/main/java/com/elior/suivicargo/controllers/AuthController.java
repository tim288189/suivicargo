package com.elior.suivicargo.controllers;

import com.elior.suivicargo.dtos.AuthResponse;
import com.elior.suivicargo.dtos.LoginRequest;
import com.elior.suivicargo.dtos.RegisterRequest;
import com.elior.suivicargo.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentification et inscription")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Se connecter avec email + mot de passe")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Inscrire un nouvel utilisateur (ADMIN seulement)")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }
}
