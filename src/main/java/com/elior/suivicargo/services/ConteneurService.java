package com.elior.suivicargo.services;

import com.elior.suivicargo.dtos.ConteneurDto;
import com.elior.suivicargo.dtos.CreateConteneurRequest;
import com.elior.suivicargo.dtos.UpdateConteneurRequest;
import com.elior.suivicargo.exceptions.BusinessException;
import com.elior.suivicargo.mappers.ConteneurMapper;
import com.elior.suivicargo.models.Conteneur;
import com.elior.suivicargo.models.Voyage;
import com.elior.suivicargo.repositories.ConteneurRepository;
import com.elior.suivicargo.repositories.VoyageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConteneurService {

    private final ConteneurRepository repository;
    private final VoyageRepository voyageRepository;
    private final ConteneurMapper mapper;

    @Transactional
    public ConteneurDto create(CreateConteneurRequest req) {
        if (repository.existsByNumero(req.numero())) {
            throw BusinessException.conflict("CONTENEUR_NUMERO_TAKEN",
                    "Ce numéro de conteneur existe déjà : " + req.numero());
        }

        Voyage voyage = null;
        if (req.voyageId() != null) {
            voyage = voyageRepository.findById(req.voyageId())
                    .orElseThrow(() -> BusinessException.notFound("VOYAGE_NOT_FOUND",
                            "Voyage introuvable : " + req.voyageId()));
        }

        Conteneur c = Conteneur.builder()
                .numero(req.numero())
                .typeConteneur(req.typeConteneur())
                .voyage(voyage)
                .build();
        return mapper.toDto(repository.save(c));
    }

    @Transactional(readOnly = true)
    public Page<ConteneurDto> list(Long voyageId, Pageable pageable) {
        if (voyageId != null) {
            return repository.findByVoyageIdAndSupprimeFalse(voyageId, pageable).map(mapper::toDto);
        }
        return repository.findBySupprimeFalse(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public ConteneurDto getById(Long id) {
        return mapper.toDto(findOrThrow(id));
    }

    @Transactional
    public ConteneurDto update(Long id, UpdateConteneurRequest req) {
        Conteneur c = findOrThrow(id);
        if (req.typeConteneur() != null) c.setTypeConteneur(req.typeConteneur());
        if (req.voyageId() != null) {
            Voyage voyage = voyageRepository.findById(req.voyageId())
                    .orElseThrow(() -> BusinessException.notFound("VOYAGE_NOT_FOUND",
                            "Voyage introuvable : " + req.voyageId()));
            c.setVoyage(voyage);
        }
        return mapper.toDto(repository.save(c));
    }

    @Transactional
    public void softDelete(Long id) {
        Conteneur c = findOrThrow(id);
        c.setSupprime(true);
        repository.save(c);
    }

    private Conteneur findOrThrow(Long id) {
        Conteneur c = repository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("CONTENEUR_NOT_FOUND",
                        "Conteneur introuvable : " + id));
        if (c.isSupprime()) {
            throw BusinessException.notFound("CONTENEUR_NOT_FOUND", "Conteneur supprimé : " + id);
        }
        return c;
    }
}
