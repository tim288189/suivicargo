package com.elior.suivicargo.services;

import com.elior.suivicargo.dtos.ConteneurDto;
import com.elior.suivicargo.dtos.CreateConteneurRequest;
import com.elior.suivicargo.exceptions.BusinessException;
import com.elior.suivicargo.mappers.ConteneurMapper;
import com.elior.suivicargo.models.Conteneur;
import com.elior.suivicargo.models.Voyage;
import com.elior.suivicargo.repositories.ConteneurRepository;
import com.elior.suivicargo.repositories.VoyageRepository;
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
class ConteneurServiceTest {

    @Mock private ConteneurRepository repository;
    @Mock private VoyageRepository voyageRepository;
    @Mock private ConteneurMapper mapper;

    @InjectMocks private ConteneurService service;

    @Test
    @DisplayName("create() refuse si numéro déjà utilisé")
    void create_numeroConflit() {
        CreateConteneurRequest req = new CreateConteneurRequest("MSCU1234567", "40HC", null);
        when(repository.existsByNumero("MSCU1234567")).thenReturn(true);

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("conteneur");
    }

    @Test
    @DisplayName("create() refuse si voyageId fourni mais introuvable")
    void create_voyageNotFound() {
        CreateConteneurRequest req = new CreateConteneurRequest("MSCU1234567", "40HC", 99L);
        when(repository.existsByNumero("MSCU1234567")).thenReturn(false);
        when(voyageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Voyage introuvable");
    }

    @Test
    @DisplayName("create() ok sans voyage")
    void create_ok_sansVoyage() {
        CreateConteneurRequest req = new CreateConteneurRequest("MSCU1234567", "40HC", null);
        when(repository.existsByNumero("MSCU1234567")).thenReturn(false);
        when(repository.save(any(Conteneur.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDto(any())).thenReturn(mock(ConteneurDto.class));

        service.create(req);

        verify(repository).save(argThat(c ->
                "MSCU1234567".equals(c.getNumero()) && c.getVoyage() == null
        ));
    }

    @Test
    @DisplayName("create() ok avec voyage rattaché")
    void create_ok_avecVoyage() {
        CreateConteneurRequest req = new CreateConteneurRequest("MSCU1234567", "20", 5L);
        Voyage v = new Voyage();
        when(repository.existsByNumero("MSCU1234567")).thenReturn(false);
        when(voyageRepository.findById(5L)).thenReturn(Optional.of(v));
        when(repository.save(any(Conteneur.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDto(any())).thenReturn(mock(ConteneurDto.class));

        service.create(req);

        verify(repository).save(argThat(c -> c.getVoyage() == v));
    }
}
