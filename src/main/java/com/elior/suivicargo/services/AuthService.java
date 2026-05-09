package com.elior.suivicargo.services;

import com.elior.suivicargo.dtos.AuthResponse;
import com.elior.suivicargo.dtos.LoginRequest;
import com.elior.suivicargo.dtos.RegisterRequest;
import com.elior.suivicargo.exceptions.BusinessException;
import com.elior.suivicargo.models.User;
import com.elior.suivicargo.repositories.UserRepository;
import com.elior.suivicargo.security.JwtProperties;
import com.elior.suivicargo.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));

        User u = userRepository.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> BusinessException.notFound("USER_NOT_FOUND", "Utilisateur introuvable"));

        return buildResponse(u);
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmailIgnoreCase(req.email())) {
            throw BusinessException.conflict("EMAIL_TAKEN", "Cet email est déjà utilisé");
        }

        User u = User.builder()
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .nom(req.nom())
                .prenom(req.prenom())
                .telephone(req.telephone())
                .role(req.role())
                .actif(true)
                .build();

        userRepository.save(u);
        return buildResponse(u);
    }

    private AuthResponse buildResponse(User u) {
        String token = jwtService.generate(u.getEmail(), u.getId(), u.getRole());
        return new AuthResponse(
                token,
                "Bearer",
                jwtProperties.expirationMinutes(),
                new AuthResponse.UserInfo(u.getId(), u.getEmail(), u.getNom(), u.getPrenom(), u.getRole())
        );
    }
}
