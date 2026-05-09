package com.elior.suivicargo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test du contexte Spring.
 *
 * <p>Remplacé en pratique par {@link com.elior.suivicargo.integration.AuthFlowIntegrationTest}
 * qui couvre déjà le démarrage complet via Testcontainers.</p>
 */
@SpringBootTest
@ActiveProfiles("dev")
@Disabled("Couvert par AuthFlowIntegrationTest (Testcontainers MySQL).")
class SuivicargoApplicationTests {

    @Test
    void contextLoads() {
        // Spring démarre l'app — si le contexte ne charge pas, le test échoue.
    }
}
