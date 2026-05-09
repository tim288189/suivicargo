package com.elior.suivicargo.repositories;

import com.elior.suivicargo.models.Facture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> {

    Optional<Facture> findByCargaisonId(Long cargaisonId);

    Optional<Facture> findByNumero(String numero);

    @Query("SELECT COUNT(f) FROM Facture f WHERE YEAR(f.dateFacture) = :annee")
    long countByAnnee(@Param("annee") int annee);
}
