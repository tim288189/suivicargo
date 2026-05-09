package com.elior.suivicargo.repositories;

import com.elior.suivicargo.models.PlanPaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanPaiementRepository extends JpaRepository<PlanPaiement, Long> {
    Optional<PlanPaiement> findByCargaisonId(Long cargaisonId);
}
