package com.elior.suivicargo.repositories;

import com.elior.suivicargo.enums.StatutCargaison;
import com.elior.suivicargo.models.Cargaison;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CargaisonRepository extends JpaRepository<Cargaison, Long> {

    Optional<Cargaison> findByNumeroTracage(String numeroTracage);

    boolean existsByNumeroTracage(String numeroTracage);

    Page<Cargaison> findByStatutAndSupprimeFalse(StatutCargaison statut, Pageable pageable);

    Page<Cargaison> findBySupprimeFalse(Pageable pageable);

    Page<Cargaison> findByClientIdAndSupprimeFalse(Long clientId, Pageable pageable);

    @Query("""
        SELECT c FROM Cargaison c
         WHERE c.supprime = false
           AND c.statut NOT IN (com.elior.suivicargo.enums.StatutCargaison.LIVRE,
                                com.elior.suivicargo.enums.StatutCargaison.ANNULE)
    """)
    Page<Cargaison> findEnCours(Pageable pageable);

    @Query("""
        SELECT COUNT(c) FROM Cargaison c
         WHERE c.supprime = false
           AND YEAR(c.dateCreation) = :annee
    """)
    long countByAnnee(@Param("annee") int annee);

    /** Cargaisons affectées au voyage soit directement (voyage_id), soit via leur conteneur. */
    @Query("""
        SELECT DISTINCT c FROM Cargaison c
         WHERE (c.voyage.id = :voyageId OR c.conteneur.voyage.id = :voyageId)
           AND c.supprime = false
    """)
    List<Cargaison> findByVoyageId(@Param("voyageId") Long voyageId);

    Page<Cargaison> findByVoyageIdAndSupprimeFalse(Long voyageId, Pageable pageable);
}
