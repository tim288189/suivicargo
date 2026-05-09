package com.elior.suivicargo.services;

import com.elior.suivicargo.repositories.CargaisonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Génère un numéro de traçage unique au format PREFIX-YYYY-NNNNNN.
 *
 * <p><b>Note de concurrence :</b> sur un volume modéré, le compteur basé sur
 * COUNT()+1 suffit. Si le volume devient élevé, remplacer par une séquence MySQL
 * (ex: table dédiée numero_sequence avec UPDATE atomique) pour éviter les collisions.
 */
@Service
@RequiredArgsConstructor
public class NumeroTracageService {

    private final CargaisonRepository repository;

    @Value("${app.tracking.prefix:MAR}")
    private String prefix;

    public String genererNumero() {
        int annee = LocalDate.now().getYear();
        long count = repository.countByAnnee(annee) + 1;

        String numero;
        do {
            numero = String.format("%s-%d-%06d", prefix, annee, count);
            count++;
        } while (repository.existsByNumeroTracage(numero));

        return numero;
    }
}
