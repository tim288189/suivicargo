package com.elior.suivicargo.repositories;

import com.elior.suivicargo.models.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByTelephone(String telephone);

    @Query("""
        SELECT c FROM Client c
         WHERE c.supprime = false
           AND ( :q IS NULL OR
                 LOWER(c.nom)       LIKE LOWER(CONCAT('%', :q, '%')) OR
                 LOWER(c.prenom)    LIKE LOWER(CONCAT('%', :q, '%')) OR
                 c.telephone        LIKE CONCAT('%', :q, '%')
               )
    """)
    Page<Client> search(@Param("q") String q, Pageable pageable);
}
