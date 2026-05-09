package com.elior.suivicargo.services;

import com.elior.suivicargo.dtos.AuthResponse;
import com.elior.suivicargo.dtos.LoginRequest;
import com.elior.suivicargo.dtos.RegisterRequest;
import com.elior.suivicargo.enums.Role;
import com.elior.suivicargo.exceptions.BusinessException;
import com.elior.suivicargo.models.User;
import com.elior.suivicargo.repositories.UserRepository;
import com.elior.suivicargo.security.JwtProperties;
import com.elior.suivicargo.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private JwtProperties jwtProperties;

    @InjectMocks private AuthService service;

    @Test
    @DisplayName("login() retourne un AuthResponse valide")
    void login_ok() {
        LoginRequest req = new LoginRequest("admin@x.com", "pass");
        User u = User.builder().email("admin@x.com").nom("A").prenom("B").role(Role.ADMIN).actif(true).build();
        u.setId(1L);

        when(userRepository.findByEmailIgnoreCase("admin@x.com")).thenReturn(Optional.of(u));
        when(jwtService.generate("admin@x.com", 1L, Role.ADMIN)).thenReturn("token123");
        when(jwtProperties.expirationMinutes()).thenReturn(120L);

        AuthResponse res = service.login(req);

        assertThat(res.token()).isEqualTo("token123");
        assertThat(res.tokenType()).isEqualTo("Bearer");
        assertThat(res.expiresInMinutes()).isEqualTo(120L);
        assertThat(res.user().email()).isEqualTo("admin@x.com");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("login() propage BadCredentialsException si mauvais mot de passe")
    void login_badCredentials() {
        LoginRequest req = new LoginRequest("admin@x.com", "wrong");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> service.login(req)).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("register() refuse un email déjà utilisé")
    void register_emailTaken() {
        RegisterRequest req = new RegisterRequest("a@b.com", "password", "N", "P", null, Role.EMPLOYEE);
        when(userRepository.existsByEmailIgnoreCase("a@b.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("déjà utilisé");
    }

    @Test
    @DisplayName("register() hash le mot de passe et sauvegarde")
    void register_ok() {
        RegisterRequest req = new RegisterRequest("a@b.com", "password", "Nom", "Prenom", "0123", Role.EMPLOYEE);
        when(userRepository.existsByEmailIgnoreCase("a@b.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("HASH");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generate(any(), any(), any())).thenReturn("token");
        when(jwtProperties.expirationMinutes()).thenReturn(60L);

        AuthResponse res = service.register(req);

        assertThat(res.user().email()).isEqualTo("a@b.com");
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(argThat(u -> "HASH".equals(u.getPasswordHash()) && u.isActif()));
    }
}
