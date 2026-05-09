package com.elior.suivicargo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Configuration OpenAPI :
 * <ul>
 *   <li>Métadonnées globales (titre, version, sécurité)</li>
 *   <li>Schéma de sécurité Bearer JWT</li>
 *   <li>Expose le contrat statique <code>openapi.yaml</code> sur <code>/openapi/openapi.yaml</code>
 *       pour le générateur de client front (Orval).</li>
 * </ul>
 */
@Configuration
public class OpenApiConfig implements WebMvcConfigurer {

    private static final String SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI suivicargoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Suivicargo API")
                        .description("API de gestion et de suivi de cargaison par fret maritime.")
                        .version("1.0.0")
                        .contact(new Contact().name("Equipe Suivicargo"))
                        .license(new License().name("Proprietary")))
                .servers(List.of(
                        new Server().url("/").description("Default")
                ))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SCHEME_NAME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT obtenu via POST /v1/auth/login")));
    }

    /**
     * Sert le fichier <code>src/main/resources/openapi/openapi.yaml</code>
     * (contrat statique source de vérité) sur l'URL <code>/openapi/openapi.yaml</code>.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/openapi/**")
                .addResourceLocations("classpath:/openapi/")
                .setCachePeriod(0);
    }
}
