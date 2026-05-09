package com.elior.suivicargo.services;

import com.elior.suivicargo.dtos.ClientDto;
import com.elior.suivicargo.dtos.CreateClientRequest;
import com.elior.suivicargo.exceptions.BusinessException;
import com.elior.suivicargo.mappers.ClientMapper;
import com.elior.suivicargo.models.Client;
import com.elior.suivicargo.repositories.ClientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock private ClientRepository repository;
    @Mock private ClientMapper mapper;
    @InjectMocks private ClientService service;

    @Test
    @DisplayName("create() délègue à mapper + repository et renvoie le DTO")
    void create_ok() {
        CreateClientRequest req = new CreateClientRequest(
                "Doe", "John", "+221701234567", "j@d.com",
                "Adresse 1", "Adresse 2");
        Client entity = new Client();
        Client saved  = new Client();
        ClientDto dto = new ClientDto(1L, "Doe", "John", "+221701234567", "j@d.com",
                "A1", "A2", null);

        when(mapper.toEntity(req)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(dto);

        ClientDto result = service.create(req);

        assertThat(result).isEqualTo(dto);
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("getById() jette NOT_FOUND si client absent")
    void getById_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Client introuvable");
    }

    @Test
    @DisplayName("getById() jette NOT_FOUND si client soft-deleted")
    void getById_supprime() {
        Client c = new Client();
        c.setSupprime(true);
        when(repository.findById(1L)).thenReturn(Optional.of(c));

        assertThatThrownBy(() -> service.getById(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("supprimé");
    }

    @Test
    @DisplayName("softDelete() positionne le flag et sauvegarde")
    void softDelete_ok() {
        Client c = new Client();
        when(repository.findById(1L)).thenReturn(Optional.of(c));

        service.softDelete(1L);

        assertThat(c.isSupprime()).isTrue();
        verify(repository).save(c);
    }

    @Test
    @DisplayName("update() applique le mapper sur l'entité existante")
    void update_ok() {
        Client c = new Client();
        when(repository.findById(1L)).thenReturn(Optional.of(c));
        when(repository.save(c)).thenReturn(c);
        when(mapper.toDto(c)).thenReturn(new ClientDto(1L, null, null, null, null, null, null, null));

        service.update(1L, null);

        verify(mapper).updateEntity(any(), eq(c));
        verify(repository).save(c);
    }
}
