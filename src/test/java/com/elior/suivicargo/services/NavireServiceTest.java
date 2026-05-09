package com.elior.suivicargo.services;

import com.elior.suivicargo.dtos.CreateNavireRequest;
import com.elior.suivicargo.dtos.NavireDto;
import com.elior.suivicargo.exceptions.BusinessException;
import com.elior.suivicargo.mappers.NavireMapper;
import com.elior.suivicargo.models.Navire;
import com.elior.suivicargo.repositories.NavireRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NavireServiceTest {

    @Mock private NavireRepository repository;
    @Mock private NavireMapper mapper;
    @InjectMocks private NavireService service;

    @Test
    @DisplayName("create() refuse un IMO déjà utilisé (CONFLICT)")
    void create_imoConflit() {
        CreateNavireRequest req = new CreateNavireRequest("MSC", "1234567", "FR", 5000);
        when(repository.existsByImo("1234567")).thenReturn(true);

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("IMO");
    }

    @Test
    @DisplayName("create() ok : appelle mapper, sauve et retourne DTO")
    void create_ok() {
        CreateNavireRequest req = new CreateNavireRequest("MSC", "1234567", "FR", 5000);
        Navire entity = new Navire();
        Navire saved  = new Navire();
        NavireDto dto = new NavireDto(1L, "MSC", "1234567", "FR", 5000);

        when(repository.existsByImo("1234567")).thenReturn(false);
        when(mapper.toEntity(req)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(dto);

        service.create(req);

        verify(repository).save(entity);
    }

    @Test
    @DisplayName("getById() jette NOT_FOUND si supprimé")
    void getById_supprime() {
        Navire n = new Navire();
        n.setSupprime(true);
        when(repository.findById(1L)).thenReturn(Optional.of(n));

        assertThatThrownBy(() -> service.getById(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("softDelete() positionne supprime=true")
    void softDelete_ok() {
        Navire n = new Navire();
        when(repository.findById(1L)).thenReturn(Optional.of(n));

        service.softDelete(1L);

        verify(repository).save(argThat(Navire::isSupprime));
    }

    @Test
    @DisplayName("update() applique le mapper sur l'entité")
    void update_ok() {
        Navire n = new Navire();
        when(repository.findById(1L)).thenReturn(Optional.of(n));
        when(repository.save(n)).thenReturn(n);
        when(mapper.toDto(n)).thenReturn(mock(NavireDto.class));

        service.update(1L, null);

        verify(mapper).updateEntity(any(), eq(n));
    }
}
