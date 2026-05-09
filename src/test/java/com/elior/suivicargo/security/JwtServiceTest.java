package com.elior.suivicargo.security;

import com.elior.suivicargo.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        // Clé HMAC-SHA256 de 32+ chars (256 bits)
        JwtProperties props = new JwtProperties(
                "this-is-a-very-long-test-secret-key-32+chars-min",
                60,
                "suivicargo");
        jwtService = new JwtService(props);
    }

    @Test
    @DisplayName("Le token généré contient subject, uid et role")
    void generateAndParse() {
        String token = jwtService.generate("a@b.com", 7L, Role.ADMIN);

        Claims claims = jwtService.parse(token);

        assertThat(claims.getSubject()).isEqualTo("a@b.com");
        assertThat(claims.get("uid", Long.class)).isEqualTo(7L);
        assertThat(claims.get("role")).isEqualTo("ADMIN");
        assertThat(claims.getIssuer()).isEqualTo("suivicargo");
    }

    @Test
    @DisplayName("parse() rejette un token invalide")
    void parse_invalid() {
        assertThatThrownBy(() -> jwtService.parse("not-a-jwt"))
                .isInstanceOf(JwtException.class);
    }
}
