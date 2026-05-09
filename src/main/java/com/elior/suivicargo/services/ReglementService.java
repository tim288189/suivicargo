package com.elior.suivicargo.services;

import com.elior.suivicargo.dtos.CreateReglementRequest;
import com.elior.suivicargo.dtos.ReglementDto;
import com.elior.suivicargo.enums.StatutPaiement;
import com.elior.suivicargo.events.CargaisonSoldeeEvent;
import com.elior.suivicargo.exceptions.BusinessException;
import com.elior.suivicargo.mappers.ReglementMapper;
import com.elior.suivicargo.models.Cargaison;
import com.elior.suivicargo.models.Echeance;
import com.elior.suivicargo.models.PlanPaiement;
import com.elior.suivicargo.models.Reglement;
import com.elior.suivicargo.models.User;
import com.elior.suivicargo.repositories.CargaisonRepository;
import com.elior.suivicargo.repositories.EcheanceRepository;
import com.elior.suivicargo.repositories.PlanPaiementRepository;
import com.elior.suivicargo.repositories.ReglementRepository;
import com.elior.suivicargo.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReglementService {

    private final ReglementRepository reglementRepository;
    private final PlanPaiementRepository planRepository;
    private final EcheanceRepository echeanceRepository;
    private final CargaisonRepository cargaisonRepository;
    private final UserRepository userRepository;
    private final ReglementMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Enregistre un règlement reçu :
     *   - rattache au plan de paiement de la cargaison
     *   - met à jour la (les) échéance(s) couverte(s)
     *   - met à jour le montant réglé sur la cargaison
     *   - publie un événement CargaisonSoldeeEvent si solde atteint
     *
     * Le plan de paiement est OPTIONNEL — un règlement peut être saisi à l'enlèvement
     * sans plan préalable (l'employé renseigne juste le montant reçu, il vient s'imputer
     * directement sur le montant réglé de la cargaison).
     */
    @Transactional
    public ReglementDto enregistrer(CreateReglementRequest req) {
        Cargaison cargaison = cargaisonRepository.findById(req.cargaisonId())
                .orElseThrow(() -> BusinessException.notFound("CARGAISON_NOT_FOUND",
                        "Cargaison introuvable : " + req.cargaisonId()));

        BigDecimal nouveauMontantRegle = cargaison.getMontantRegle().add(req.montant());
        if (nouveauMontantRegle.compareTo(cargaison.getMontantTotal()) > 0) {
            throw BusinessException.badRequest("MONTANT_DEPASSE_TOTAL",
                    "Le montant total réglé dépasserait le montant total de la cargaison");
        }

        PlanPaiement plan = planRepository.findByCargaisonId(cargaison.getId()).orElse(null);
        Echeance echeance = resolveEcheance(plan, req.echeanceId());

        User encaissePar = currentUser();

        Reglement r = Reglement.builder()
                .planPaiement(plan) // peut être null si pas de plan
                .echeance(echeance)
                .montant(req.montant())
                .modePaiement(req.modePaiement())
                .referenceTransaction(req.referenceTransaction())
                .dateReglement(req.dateReglement())
                .encaissePar(encaissePar)
                .commentaire(req.commentaire())
                .build();
        r = reglementRepository.save(r);

        // 1. Mise à jour du montant réglé sur la cargaison
        cargaison.setMontantRegle(nouveauMontantRegle);
        cargaisonRepository.save(cargaison);

        // 2. Si plan présent, mise à jour des statuts d'échéances + statut global
        if (plan != null) {
            recalculerStatutsEcheances(plan);
            recalculerStatutPlan(plan);
        }

        // 3. Si soldé, publier l'événement (déclenche la facture)
        if (cargaison.isSolde() && !cargaison.isFactureEnvoyee()) {
            log.info("Cargaison {} soldée — publication CargaisonSoldeeEvent",
                    cargaison.getNumeroTracage());
            eventPublisher.publishEvent(new CargaisonSoldeeEvent(cargaison.getId()));
        }

        return mapper.toDto(r);
    }

    @Transactional(readOnly = true)
    public Page<ReglementDto> listByCargaison(Long cargaisonId, Pageable pageable) {
        PlanPaiement plan = planRepository.findByCargaisonId(cargaisonId).orElse(null);
        if (plan == null) {
            return Page.empty(pageable);
        }
        return reglementRepository.findByPlanPaiementId(plan.getId(), pageable).map(mapper::toDto);
    }

    private Echeance resolveEcheance(PlanPaiement plan, Long echeanceId) {
        if (plan == null) return null;
        if (echeanceId != null) {
            return echeanceRepository.findById(echeanceId)
                    .filter(e -> e.getPlanPaiement().getId().equals(plan.getId()))
                    .orElseThrow(() -> BusinessException.badRequest("ECHEANCE_INVALIDE",
                            "L'échéance ne correspond pas au plan de paiement de la cargaison"));
        }
        // Pas d'échéance précisée : on prend la prochaine non payée
        return plan.getEcheances().stream()
                .filter(e -> e.getStatut() != StatutPaiement.PAYE)
                .findFirst()
                .orElse(null);
    }

    private void recalculerStatutsEcheances(PlanPaiement plan) {
        List<Echeance> echeances = plan.getEcheances();
        BigDecimal restant = reglementRepository.sommeReglementsDuPlan(plan.getId());

        for (Echeance e : echeances) {
            if (restant.compareTo(e.getMontantPrevu()) >= 0) {
                e.setStatut(StatutPaiement.PAYE);
                restant = restant.subtract(e.getMontantPrevu());
            } else if (restant.compareTo(BigDecimal.ZERO) > 0) {
                e.setStatut(StatutPaiement.PARTIEL);
                restant = BigDecimal.ZERO;
            } else {
                e.setStatut(StatutPaiement.EN_ATTENTE);
            }
            echeanceRepository.save(e);
        }
    }

    private void recalculerStatutPlan(PlanPaiement plan) {
        boolean toutesPayees = plan.getEcheances().stream()
                .allMatch(e -> e.getStatut() == StatutPaiement.PAYE);
        boolean aucunePaiee = plan.getEcheances().stream()
                .allMatch(e -> e.getStatut() == StatutPaiement.EN_ATTENTE);

        if (toutesPayees) {
            plan.setStatut(StatutPaiement.PAYE);
        } else if (aucunePaiee) {
            plan.setStatut(StatutPaiement.EN_ATTENTE);
        } else {
            plan.setStatut(StatutPaiement.PARTIEL);
        }
        planRepository.save(plan);
    }

    private User currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        return userRepository.findByEmailIgnoreCase(auth.getName()).orElse(null);
    }
}
