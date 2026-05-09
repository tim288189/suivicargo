package com.elior.suivicargo.services;

import com.elior.suivicargo.dtos.CargaisonDetailDto;
import com.elior.suivicargo.dtos.CargaisonDto;
import com.elior.suivicargo.dtos.CreateCargaisonRequest;
import com.elior.suivicargo.dtos.EvenementHistoriqueDto;
import com.elior.suivicargo.dtos.TrackingPublicResponse;
import com.elior.suivicargo.enums.StatutCargaison;
import com.elior.suivicargo.exceptions.BusinessException;
import com.elior.suivicargo.mappers.CargaisonMapper;
import com.elior.suivicargo.models.Cargaison;
import com.elior.suivicargo.models.Client;
import com.elior.suivicargo.models.HistoriqueStatut;
import com.elior.suivicargo.repositories.CargaisonRepository;
import com.elior.suivicargo.repositories.ClientRepository;
import com.elior.suivicargo.repositories.HistoriqueStatutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CargaisonService {

    /** Délai par défaut de livraison estimée à compter de l'enlèvement. */
    private static final int DEFAUT_JOURS_LIVRAISON = 30;

    private final CargaisonRepository cargaisonRepository;
    private final HistoriqueStatutRepository historiqueRepository;
    private final ClientRepository clientRepository;
    private final NumeroTracageService numeroTracageService;
    private final CargaisonMapper mapper;

    @Transactional
    public CargaisonDto create(CreateCargaisonRequest req) {
        Client client = clientRepository.findById(req.clientId())
                .orElseThrow(() -> BusinessException.notFound("CLIENT_NOT_FOUND",
                        "Client introuvable : " + req.clientId()));

        if (req.montantRegle() != null
                && req.montantRegle().compareTo(req.montantTotal()) > 0) {
            throw BusinessException.badRequest("MONTANT_REGLE_INVALIDE",
                    "Le montant réglé ne peut pas dépasser le montant total");
        }

        LocalDate today = LocalDate.now();

        Cargaison c = Cargaison.builder()
                .numeroTracage(numeroTracageService.genererNumero())
                .client(client)
                .nombreColis(req.nombreColis())
                .poidsKg(req.poidsKg())
                .volumeM3(req.volumeM3())
                .montantTotal(req.montantTotal())
                .montantRegle(req.montantRegle() != null ? req.montantRegle() : BigDecimal.ZERO)
                .devise(req.devise() != null ? req.devise() : "XOF")
                .statut(StatutCargaison.ENLEVE)
                .observations(req.observations())
                .factureEnvoyee(false)
                .dateEnlevement(today)
                .dateLivraisonEstimee(today.plusDays(DEFAUT_JOURS_LIVRAISON))
                .build();

        c = cargaisonRepository.save(c);

        historiqueRepository.save(HistoriqueStatut.builder()
                .cargaison(c)
                .ancienStatut(null)
                .nouveauStatut(StatutCargaison.ENLEVE)
                .commentaire("Cargaison créée à l'enlèvement")
                .auteur(currentUserName())
                .build());

        return mapper.toDto(c);
    }

    @Transactional(readOnly = true)
    public CargaisonDto getById(Long id) {
        Cargaison c = cargaisonRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("CARGAISON_NOT_FOUND",
                        "Cargaison introuvable : " + id));
        return mapper.toDto(c);
    }

    /**
     * Vue complète : cargaison + historique chronologique des statuts.
     */
    @Transactional(readOnly = true)
    public CargaisonDetailDto getDetailById(Long id) {
        Cargaison c = cargaisonRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("CARGAISON_NOT_FOUND",
                        "Cargaison introuvable : " + id));
        List<HistoriqueStatut> histo =
                historiqueRepository.findByCargaisonIdOrderByDateChangementAsc(c.getId());
        List<EvenementHistoriqueDto> historiqueDtos = mapper.toHistoriqueDtos(histo);
        return mapper.toDetailDto(c, historiqueDtos);
    }

    @Transactional(readOnly = true)
    public Page<CargaisonDto> listEnCours(Pageable pageable) {
        return cargaisonRepository.findEnCours(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<CargaisonDto> listAll(Pageable pageable) {
        return cargaisonRepository.findBySupprimeFalse(pageable).map(mapper::toDto);
    }

    @Transactional
    public CargaisonDto changerStatut(Long id, StatutCargaison nouveau, String commentaire) {
        Cargaison c = cargaisonRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("CARGAISON_NOT_FOUND",
                        "Cargaison introuvable : " + id));

        StatutCargaison ancien = c.getStatut();
        if (ancien == nouveau) {
            return mapper.toDto(c);
        }

        c.setStatut(nouveau);
        // Quand la cargaison passe en LIVRE, on horodate la livraison réelle.
        if (nouveau == StatutCargaison.LIVRE && c.getDateLivraisonReelle() == null) {
            c.setDateLivraisonReelle(LocalDate.now());
        }
        cargaisonRepository.save(c);

        historiqueRepository.save(HistoriqueStatut.builder()
                .cargaison(c)
                .ancienStatut(ancien)
                .nouveauStatut(nouveau)
                .commentaire(commentaire)
                .auteur(currentUserName())
                .build());

        return mapper.toDto(c);
    }

    @Transactional(readOnly = true)
    public TrackingPublicResponse trackPublic(String numeroTracage) {
        Cargaison c = cargaisonRepository.findByNumeroTracage(numeroTracage)
                .orElseThrow(() -> BusinessException.notFound("TRACKING_NOT_FOUND",
                        "Aucune cargaison ne correspond à ce numéro"));

        List<HistoriqueStatut> histo =
                historiqueRepository.findByCargaisonIdOrderByDateChangementAsc(c.getId());

        var events = histo.stream()
                .map(h -> new TrackingPublicResponse.EvenementTracking(
                        h.getNouveauStatut(),
                        h.getDateChangement(),
                        h.getCommentaire()))
                .toList();

        String portDepart = null;
        String portArrivee = null;
        if (c.getConteneur() != null && c.getConteneur().getVoyage() != null) {
            portDepart  = c.getConteneur().getVoyage().getPortDepart();
            portArrivee = c.getConteneur().getVoyage().getPortArrivee();
        }

        return new TrackingPublicResponse(
                c.getNumeroTracage(),
                c.getStatut(),
                c.getNombreColis(),
                portDepart,
                portArrivee,
                events
        );
    }

    private String currentUserName() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
}
