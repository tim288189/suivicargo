package com.elior.suivicargo.events;

import com.elior.suivicargo.models.Cargaison;
import com.elior.suivicargo.models.Facture;
import com.elior.suivicargo.repositories.CargaisonRepository;
import com.elior.suivicargo.services.EmailService;
import com.elior.suivicargo.services.FactureService;
import com.elior.suivicargo.services.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Réagit aux événements métier liés aux cargaisons :
 *   - CargaisonSoldeeEvent : génère la facture, envoie email + flag de suivi
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CargaisonEventListener {

    private final CargaisonRepository cargaisonRepository;
    private final FactureService factureService;
    private final EmailService emailService;
    private final TemplateService templateService;

    /**
     * Réagit en transaction séparée — si l'envoi d'email échoue, le règlement
     * principal n'est pas rollback.
     */
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onCargaisonSoldee(CargaisonSoldeeEvent event) {
        Cargaison cargaison = cargaisonRepository.findById(event.cargaisonId()).orElse(null);
        if (cargaison == null) {
            log.warn("CargaisonSoldeeEvent — cargaison {} introuvable", event.cargaisonId());
            return;
        }
        if (cargaison.isFactureEnvoyee()) {
            log.info("Facture déjà envoyée pour cargaison {}, on ignore.",
                    cargaison.getNumeroTracage());
            return;
        }

        try {
            // 1. Générer ou récupérer la facture
            Facture facture = factureService.genererOuObtenir(cargaison.getId());

            // 2. Générer le PDF
            byte[] pdf = factureService.renderPdf(facture);

            // 3. Préparer le corps d'email
            Map<String, Object> model = new HashMap<>();
            model.put("facture", Map.of(
                    "numero", facture.getNumero(),
                    "dateFacture", facture.getDateFacture().toString(),
                    "montantTtc", facture.getMontantTtc(),
                    "devise", facture.getDevise()
            ));
            model.put("cargaison", Map.of("numeroTracage", cargaison.getNumeroTracage()));
            model.put("client", Map.of(
                    "nom",    cargaison.getClient().getNom(),
                    "prenom", cargaison.getClient().getPrenom()
            ));
            String body = templateService.render("facture-email.ftl", model);

            // 4. Envoyer si on a un email client
            String to = cargaison.getClient().getEmail();
            if (to != null && !to.isBlank()) {
                emailService.envoyerAvecPieceJointe(
                        to,
                        "Votre facture Suivicargo " + facture.getNumero(),
                        body,
                        "facture-" + facture.getNumero() + ".pdf",
                        pdf
                );
                factureService.marquerEnvoyeeEmail(facture.getId(), Instant.now());
            } else {
                log.warn("Aucun email client pour cargaison {} — facture générée mais non envoyée",
                        cargaison.getNumeroTracage());
            }

            // 5. Flag sur la cargaison
            cargaison.setFactureEnvoyee(true);
            cargaisonRepository.save(cargaison);

        } catch (Exception ex) {
            log.error("Échec du traitement CargaisonSoldeeEvent pour cargaison {}",
                    cargaison.getNumeroTracage(), ex);
            // On loggue mais on ne re-throw pas pour ne pas bloquer la transaction principale
        }
    }
}
