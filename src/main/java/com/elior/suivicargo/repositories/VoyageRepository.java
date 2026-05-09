package com.elior.suivicargo.repositories;

import com.elior.suivicargo.enums.StatutVoyage;
import com.elior.suivicargo.models.Voyage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoyageRepository extends JpaRepository<Voyage, Long> {
    Page<Voyage> findByNavireIdAndSupprimeFalse(Long navireId, Pageable pageable);
    Page<Voyage> findByStatutAndSupprimeFalse(StatutVoyage statut, Pageable pageable);
}
