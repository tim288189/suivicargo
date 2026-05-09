package com.elior.suivicargo.repositories;

import com.elior.suivicargo.models.Echeance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EcheanceRepository extends JpaRepository<Echeance, Long> {
    List<Echeance> findByPlanPaiementIdOrderByOrdreAsc(Long planPaiementId);
}
