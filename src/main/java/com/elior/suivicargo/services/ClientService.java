package com.elior.suivicargo.services;

import com.elior.suivicargo.dtos.ClientDto;
import com.elior.suivicargo.dtos.CreateClientRequest;
import com.elior.suivicargo.dtos.UpdateClientRequest;
import com.elior.suivicargo.exceptions.BusinessException;
import com.elior.suivicargo.mappers.ClientMapper;
import com.elior.suivicargo.models.Client;
import com.elior.suivicargo.repositories.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository repository;
    private final ClientMapper mapper;

    @Transactional
    public ClientDto create(CreateClientRequest req) {
        Client c = mapper.toEntity(req);
        return mapper.toDto(repository.save(c));
    }

    @Transactional(readOnly = true)
    public ClientDto getById(Long id) {
        return mapper.toDto(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<ClientDto> search(String q, Pageable pageable) {
        return repository.search(q, pageable).map(mapper::toDto);
    }

    @Transactional
    public ClientDto update(Long id, UpdateClientRequest req) {
        Client c = findOrThrow(id);
        mapper.updateEntity(req, c);
        return mapper.toDto(repository.save(c));
    }

    @Transactional
    public void softDelete(Long id) {
        Client c = findOrThrow(id);
        c.setSupprime(true);
        repository.save(c);
    }

    private Client findOrThrow(Long id) {
        Client c = repository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("CLIENT_NOT_FOUND",
                        "Client introuvable : " + id));
        if (c.isSupprime()) {
            throw BusinessException.notFound("CLIENT_NOT_FOUND", "Client supprimé : " + id);
        }
        return c;
    }
}
