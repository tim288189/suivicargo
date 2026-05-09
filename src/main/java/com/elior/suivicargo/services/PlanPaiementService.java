package com.elior.suivicargo.services;

import com.elior.suivicargo.dtos.CreatePlanPaiementRequest;
import com.elior.suivicargo.dtos.PlanPaiementDto;
import com.elior.suivicargo.enums.StatutPaiement;
import com.elior.suivicargo.exceptions.BusinessException;
import com.elior.suivicargo.mappers.PlanPaiementMapper;
import com.elior.suivicargo.models.Cargaison;
import com.elior.suivicargo.models.Echeance;
import com.elior.suivicargo.models.PlanPaiement;
import com.elior.suivicargo.repositories.CargaisonRepository;
import com.elior.suivicargo.repositories.PlanPaiementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PlanPaiementService {

    private final PlanPaiementRepository planRepository;
    private final CargaisonRepository cargaisonRepository;
    private final PlanPaiementMapper mapper;

    @Transactional
    public PlanPaiementDto create(CreatePlanPaiementRequest req) {
        Cargaison cargaison = cargaisonRepository.findById(req.cargaisonId())
                .orElseThrow(() -> BusinessException.notFound("CARGAISON_NOT_FOUND",
                        "Cargaison introuvable : " + req.cargaisonId()));

        if (planRepository.findByCargaisonId(req.cargaisonId()).isPresent()) {
            throw BusinessException.conflict("PLAN_EXISTS",
                    "Un plan de paiement existe déjà pour cette cargaison");
        }

        // Validation : la somme des échéances doit égaler le montant total de la cargaison
        BigDecimal sommeEcheances = req.echeances().stream()
                .map(e -> e.montantPrevu())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sommeEcheances.compareTo(cargaison.getMontantTotal()) != 0) {
            throw BusinessException.badRequest("ECHEANCES_INCOHERENTES",
                    "La somme des échéances (" + sommeEcheances
                            + ") doit égaler le montant total de la cargaison (" + cargaison.getMontantTotal() + ")");
        }

        PlanPaiement plan = PlanPaiement.builder()
                .cargaison(cargaison)
                .montantTotal(cargaison.getMontantTotal())
                .devise(req.devise() != null ? req.devise() : cargaison.getDevise())
                .statut(StatutPaiement.EN_ATTENTE)
                .build();

        req.echeances().forEach(e -> {
            Echeance ec = Echeance.builder()
                    .ordre(e.ordre())
                    .libelle(e.libelle())
                    .montantPrevu(e.montantPrevu())
                    .dateEcheance(e.dateEcheance())
                    .statut(StatutPaiement.EN_ATTENTE)
                    .build();
            plan.addEcheance(ec);
        });

        return mapper.toDto(planRepository.save(plan));
    }

    @Transactional(readOnly = true)
    public PlanPaiementDto getByCargaisonId(Long cargaisonId) {
        PlanPaiement plan = planRepository.findByCargaisonId(cargaisonId)
                .orElseThrow(() -> BusinessException.notFound("PLAN_NOT_FOUND",
                        "Aucun plan de paiement pour la cargaison " + cargaisonId));
        return mapper.toDto(plan);
    }
}
