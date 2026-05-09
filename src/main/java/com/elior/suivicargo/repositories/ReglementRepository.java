package com.elior.suivicargo.repositories;

import com.elior.suivicargo.models.Reglement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ReglementRepository extends JpaRepository<Reglement, Long> {

    List<Reglement> findByPlanPaiementIdOrderByDateReglementAsc(Long planPaiementId);

    Page<Reglement> findByPlanPaiementId(Long planPaiementId, Pageable pageable);

    @Query("""
        SELECT COALESCE(SUM(r.montant), 0)
          FROM Reglement r
         WHERE r.planPaiement.id = :planId
           AND r.supprime = false
    """)
    BigDecimal sommeReglementsDuPlan(@Param("planId") Long planId);
}
