package com.elior.suivicargo.repositories;

import com.elior.suivicargo.models.Navire;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NavireRepository extends JpaRepository<Navire, Long> {
    Optional<Navire> findByImo(String imo);
    boolean existsByImo(String imo);
    Page<Navire> findBySupprimeFalse(Pageable pageable);
}
