package com.elior.suivicargo.services;

import com.elior.suivicargo.dtos.CreateVoyageRequest;
import com.elior.suivicargo.dtos.UpdateVoyageRequest;
import com.elior.suivicargo.dtos.VoyageDto;
import com.elior.suivicargo.enums.StatutVoyage;
import com.elior.suivicargo.exceptions.BusinessException;
import com.elior.suivicargo.mappers.VoyageMapper;
import com.elior.suivicargo.models.Navire;
import com.elior.suivicargo.models.Voyage;
import com.elior.suivicargo.repositories.NavireRepository;
import com.elior.suivicargo.repositories.VoyageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VoyageService {

    private final VoyageRepository repository;
    private final NavireRepository navireRepository;
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

    @Transactional
    public VoyageDto update(Long id, UpdateVoyageRequest req) {
        Voyage v = findOrThrow(id);
        mapper.updateEntity(req, v);
        return mapper.toDto(repository.save(v));
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
}
