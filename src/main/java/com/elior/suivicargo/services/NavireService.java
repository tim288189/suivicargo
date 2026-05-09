package com.elior.suivicargo.services;

import com.elior.suivicargo.dtos.CreateNavireRequest;
import com.elior.suivicargo.dtos.NavireDto;
import com.elior.suivicargo.dtos.UpdateNavireRequest;
import com.elior.suivicargo.exceptions.BusinessException;
import com.elior.suivicargo.mappers.NavireMapper;
import com.elior.suivicargo.models.Navire;
import com.elior.suivicargo.repositories.NavireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NavireService {

    private final NavireRepository repository;
    private final NavireMapper mapper;

    @Transactional
    public NavireDto create(CreateNavireRequest req) {
        if (repository.existsByImo(req.imo())) {
            throw BusinessException.conflict("IMO_TAKEN", "Ce numéro IMO existe déjà : " + req.imo());
        }
        return mapper.toDto(repository.save(mapper.toEntity(req)));
    }

    @Transactional(readOnly = true)
    public Page<NavireDto> list(Pageable pageable) {
        return repository.findBySupprimeFalse(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public NavireDto getById(Long id) {
        return mapper.toDto(findOrThrow(id));
    }

    @Transactional
    public NavireDto update(Long id, UpdateNavireRequest req) {
        Navire n = findOrThrow(id);
        mapper.updateEntity(req, n);
        return mapper.toDto(repository.save(n));
    }

    @Transactional
    public void softDelete(Long id) {
        Navire n = findOrThrow(id);
        n.setSupprime(true);
        repository.save(n);
    }

    private Navire findOrThrow(Long id) {
        Navire n = repository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("NAVIRE_NOT_FOUND",
                        "Navire introuvable : " + id));
        if (n.isSupprime()) {
            throw BusinessException.notFound("NAVIRE_NOT_FOUND", "Navire supprimé : " + id);
        }
        return n;
    }
}
