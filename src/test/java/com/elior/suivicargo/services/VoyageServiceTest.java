package com.elior.suivicargo.services;

import com.elior.suivicargo.dtos.CreateVoyageRequest;
import com.elior.suivicargo.dtos.VoyageDto;
import com.elior.suivicargo.enums.StatutVoyage;
import com.elior.suivicargo.exceptions.BusinessException;
import com.elior.suivicargo.mappers.VoyageMapper;
import com.elior.suivicargo.models.Navire;
import com.elior.suivicargo.models.Voyage;
import com.elior.suivicargo.repositories.NavireRepository;
import com.elior.suivicargo.repositories.VoyageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoyageServiceTest {

    @Mock private VoyageRepository repository;
    @Mock private NavireRepository navireRepository;
    @Mock private VoyageMapper mapper;

    @InjectMocks private VoyageService service;

    @Test
    @DisplayName("create() refuse si ETA antérieure à la date de départ")
    void create_etaAvantDepart() {
        CreateVoyageRequest req = new CreateVoyageRequest(
                1L, "Dakar", "Le Havre",
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 5, 1));
        when(navireRepository.findById(1L)).thenReturn(Optional.of(new Navire()));

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ETA");
    }

    @Test
    @DisplayName("create() refuse si navire introuvable")
    void create_navireNotFound() {
        CreateVoyageRequest req = new CreateVoyageRequest(
                99L, "Dakar", "Le Havre", LocalDate.now(), LocalDate.now().plusDays(15));
        when(navireRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Navire introuvable");
    }

    @Test
    @DisplayName("create() ok : statut PROGRAMME par défaut")
    void create_ok() {
        CreateVoyageRequest req = new CreateVoyageRequest(
                1L, "Dakar", "Le Havre",
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 20));
        when(navireRepository.findById(1L)).thenReturn(Optional.of(new Navire()));
        when(repository.save(any(Voyage.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDto(any(Voyage.class))).thenReturn(mock(VoyageDto.class));

        service.create(req);

        verify(repository).save(argThat(v ->
                v.getStatut() == StatutVoyage.PROGRAMME
                && v.getPortDepart().equals("Dakar")
                && v.getPortArrivee().equals("Le Havre")
        ));
    }
}
