package com.elior.suivicargo.repositories;

import com.elior.suivicargo.models.HistoriqueStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriqueStatutRepository extends JpaRepository<HistoriqueStatut, Long> {
    List<HistoriqueStatut> findByCargaisonIdOrderByDateChangementAsc(Long cargaisonId);
}
