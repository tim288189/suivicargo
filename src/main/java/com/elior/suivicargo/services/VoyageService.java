package com.elior.suivicargo.services;

import com.elior.suivicargo.dtos.CreateVoyageRequest;
import com.elior.suivicargo.dtos.UpdateVoyageRequest;
import com.elior.suivicargo.dtos.VoyageDto;
import com.elior.suivicargo.enums.StatutCargaison;
import com.elior.suivicargo.enums.StatutVoyage;
import com.elior.suivicargo.exceptions.BusinessException;
import com.elior.suivicargo.mappers.VoyageMapper;
import com.elior.suivicargo.models.Cargaison;
import com.elior.suivicargo.models.HistoriqueStatut;
import com.elior.suivicargo.models.Navire;
import com.elior.suivicargo.models.Voyage;
import com.elior.suivicargo.repositories.CargaisonRepository;
import com.elior.suivicargo.repositories.HistoriqueStatutRepository;
import com.elior.suivicargo.repositories.NavireRepository;
import com.elior.suivicargo.repositories.VoyageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoyageService {

    private final VoyageRepository repository;
    private final NavireRepository navireRepository;
    private final CargaisonRepository cargaisonRepository;
    private final HistoriqueStatutRepository historiqueRepository;
    private final VoyageMapper mapper;

    @Transactional
    public VoyageDto create(CreateVoyageRequest req) {
        Navire navire = navireRepository.findById(req.navireId())
                .orElseThrow(() -> BusinessException.notFound("NAVIRE_NOT_FOUND",
                        "Navire introuvable : " + req.navireId()));

        if (req.etaArrivee().isBefore(req.dateDepart())) {
            throw BusinessException.badRequest("ETA_AVANT_DEPART",
                    "L'ETA d'arrivée ne peut pas être avant la date de départ");
        }

        Voyage v = Voyage.builder()
                .navire(navire)
                .portDepart(req.portDepart())
                .portArrivee(req.portArrivee())
                .dateDepart(req.dateDepart())
                .etaArrivee(req.etaArrivee())
                .statut(StatutVoyage.PROGRAMME)
                .build();
        return mapper.toDto(repository.save(v));
    }

    @Transactional(readOnly = true)
    public Page<VoyageDto> list(Long navireId, StatutVoyage statut, Pageable pageable) {
        if (navireId != null) {
            return repository.findByNavireIdAndSupprimeFalse(navireId, pageable).map(mapper::toDto);
        }
        if (statut != null) {
            return repository.findByStatutAndSupprimeFalse(statut, pageable).map(mapper::toDto);
        }
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public VoyageDto getById(Long id) {
        return mapper.toDto(findOrThrow(id));
    }

    /**
     * Met à jour un voyage. Si le statut change, propage le nouveau statut
     * à toutes les cargaisons rattachées (avec entrée d'historique).
     */
    @Transactional
    public VoyageDto update(Long id, UpdateVoyageRequest req) {
        Voyage v = findOrThrow(id);
        StatutVoyage ancienStatut = v.getStatut();

        mapper.updateEntity(req, v);

        // Si on passe en ARRIVE et que la date d'arrivée réelle n'est pas renseignée
        if (v.getStatut() == StatutVoyage.ARRIVE && v.getDateArriveeReelle() == null) {
            v.setDateArriveeReelle(LocalDate.now());
        }

        v = repository.save(v);

        // Propagation aux cargaisons si statut changé
        if (req.statut() != null && req.statut() != ancienStatut) {
            propagerStatutAuxCargaisons(v, ancienStatut, req.statut());
        }

        return mapper.toDto(v);
    }

    /**
     * Pour chaque cargaison rattachée à ce voyage, applique le statut cargaison
     * correspondant au nouveau statut voyage et trace dans l'historique.
     */
    private void propagerStatutAuxCargaisons(Voyage voyage,
                                             StatutVoyage ancienVoy,
                                             StatutVoyage nouveauVoy) {
        StatutCargaison nouveauCargo = mapVoyageToCargo(nouveauVoy);
        if (nouveauCargo == null) return; // pas de mapping → on ne propage pas

        List<Cargaison> cargaisons = cargaisonRepository.findByVoyageId(voyage.getId());
        if (cargaisons.isEmpty()) return;

        String auteur = currentUserName();
        String label = "Propagé depuis le voyage " + voyage.getNavire().getNom()
                + " : " + ancienVoy + " → " + nouveauVoy;

        int updated = 0;
        for (Cargaison c : cargaisons) {
            // Ne pas écraser une cargaison déjà LIVRE ou ANNULE
            if (c.getStatut() == StatutCargaison.LIVRE || c.getStatut() == StatutCargaison.ANNULE) {
                continue;
            }
            StatutCargaison ancienCargo = c.getStatut();
            if (ancienCargo == nouveauCargo) {
                continue; // déjà au bon statut
            }
            c.setStatut(nouveauCargo);
            cargaisonRepository.save(c);

            historiqueRepository.save(HistoriqueStatut.builder()
                    .cargaison(c)
                    .ancienStatut(ancienCargo)
                    .nouveauStatut(nouveauCargo)
                    .commentaire(label)
                    .auteur(auteur)
                    .build());
            updated++;
        }
        log.info("Propagation statut voyage #{}: {} cargaisons mises à jour vers {}",
                voyage.getId(), updated, nouveauCargo);
    }

    /**
     * Mapping des statuts voyage vers les statuts cargaison correspondants.
     * Retourne null si on ne propage pas (ex: PROGRAMME ne change pas l'état des cargaisons).
     */
    private StatutCargaison mapVoyageToCargo(StatutVoyage v) {
        return switch (v) {
            case PROGRAMME -> StatutCargaison.CHARGE_NAVIRE;  // chargées en attente de départ
            case EN_MER    -> StatutCargaison.EN_MER;
            case ARRIVE    -> StatutCargaison.AU_PORT_ARRIVEE;
            case ANNULE    -> StatutCargaison.ANNULE;
        };
    }

    @Transactional
    public void softDelete(Long id) {
        Voyage v = findOrThrow(id);
        v.setSupprime(true);
        repository.save(v);
    }

    private Voyage findOrThrow(Long id) {
        Voyage v = repository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("VOYAGE_NOT_FOUND",
                        "Voyage introuvable : " + id));
        if (v.isSupprime()) {
            throw BusinessException.notFound("VOYAGE_NOT_FOUND", "Voyage supprimé : " + id);
        }
        return v;
    }

    private String currentUserName() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
}
