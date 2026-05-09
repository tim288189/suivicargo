package com.elior.suivicargo.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test d'intégration end-to-end :
 *  - démarre un MySQL réel via Testcontainers
 *  - applique les migrations Flyway (V1 + V2 seed admin)
 *  - login admin → JWT → endpoint protégé
 *
 * <p>Les tests sont automatiquement skippés (via {@code Assumptions}) si Docker
 * n'est pas disponible, plutôt que de faire échouer le build.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class AuthFlowIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("suivicargo")
            .withUsername("suivicargo")
            .withPassword("suivicargo");

    @DynamicPropertySource
    static void registerMysqlProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",         mysql::getJdbcUrl);
        registry.add("spring.datasource.username",    mysql::getUsername);
        registry.add("spring.datasource.password",    mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @BeforeAll
    static void requireDocker() {
        assumeTrue(DockerClientFactory.instance().isDockerAvailable(),
                "Docker indisponible — tests Testcontainers skippés");
    }

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    @DisplayName("Login admin → JWT → endpoint protégé")
    void loginAndCallProtectedEndpoint() throws Exception {
        MvcResult loginResult = mvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@suivicargo.local","password":"admin123"}
                                """))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = body.get("token").asText();
        assertThat(token).isNotBlank();
        assertThat(body.get("user").get("role").asText()).isEqualTo("ADMIN");

        // Sans le token : 401
        mvc.perform(get("/v1/cargaisons"))
                .andExpect(status().isUnauthorized());

        // Avec le JWT : 200
        mvc.perform(get("/v1/cargaisons").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Tracking public sans auth, 404 si numéro inconnu")
    void trackingPublic_inconnu() throws Exception {
        mvc.perform(get("/v1/tracking/MAR-9999-999999"))
                .andExpect(status().isNotFound());
    }
}
