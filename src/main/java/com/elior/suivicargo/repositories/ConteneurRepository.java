package com.elior.suivicargo.repositories;

import com.elior.suivicargo.models.Conteneur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConteneurRepository extends JpaRepository<Conteneur, Long> {
    Optional<Conteneur> findByNumero(String numero);
    boolean existsByNumero(String numero);
    Page<Conteneur> findByVoyageIdAndSupprimeFalse(Long voyageId, Pageable pageable);
    Page<Conteneur> findBySupprimeFalse(Pageable pageable);
}
