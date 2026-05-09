package com.elior.suivicargo.services;

import com.elior.suivicargo.dtos.CreateEcheanceRequest;
import com.elior.suivicargo.dtos.CreatePlanPaiementRequest;
import com.elior.suivicargo.dtos.PlanPaiementDto;
import com.elior.suivicargo.exceptions.BusinessException;
import com.elior.suivicargo.mappers.PlanPaiementMapper;
import com.elior.suivicargo.models.Cargaison;
import com.elior.suivicargo.models.PlanPaiement;
import com.elior.suivicargo.repositories.CargaisonRepository;
import com.elior.suivicargo.repositories.PlanPaiementRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanPaiementServiceTest {

    @Mock private PlanPaiementRepository planRepository;
    @Mock private CargaisonRepository cargaisonRepository;
    @Mock private PlanPaiementMapper mapper;

    @InjectMocks private PlanPaiementService service;

    private Cargaison cargaison(BigDecimal total) {
        Cargaison c = new Cargaison();
        c.setMontantTotal(total);
        c.setDevise("XOF");
        return c;
    }

    @Test
    @DisplayName("create() refuse si plan déjà existant pour la cargaison")
    void create_planExiste() {
        CreatePlanPaiementRequest req = new CreatePlanPaiementRequest(
                1L, "XOF",
                List.of(new CreateEcheanceRequest(1, "L1", BigDecimal.TEN, LocalDate.now())));
        when(cargaisonRepository.findById(1L)).thenReturn(Optional.of(cargaison(BigDecimal.TEN)));
        when(planRepository.findByCargaisonId(1L)).thenReturn(Optional.of(new PlanPaiement()));

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("plan");
    }

    @Test
    @DisplayName("create() refuse si somme des échéances ≠ montant total cargaison")
    void create_montantsIncoherents() {
        CreatePlanPaiementRequest req = new CreatePlanPaiementRequest(
                1L, "XOF",
                List.of(
                        new CreateEcheanceRequest(1, "L1", BigDecimal.valueOf(50), LocalDate.now()),
                        new CreateEcheanceRequest(2, "L2", BigDecimal.valueOf(50), LocalDate.now())
                ));
        when(cargaisonRepository.findById(1L)).thenReturn(Optional.of(cargaison(BigDecimal.valueOf(200))));
        when(planRepository.findByCargaisonId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("somme des échéances");
    }

    @Test
    @DisplayName("create() ok : plan créé avec ses échéances")
    void create_ok() {
        CreatePlanPaiementRequest req = new CreatePlanPaiementRequest(
                1L, "XOF",
                List.of(
                        new CreateEcheanceRequest(1, "Enlèvement", BigDecimal.valueOf(40), LocalDate.now()),
                        new CreateEcheanceRequest(2, "Livraison",  BigDecimal.valueOf(60), LocalDate.now().plusDays(20))
                ));
        when(cargaisonRepository.findById(1L)).thenReturn(Optional.of(cargaison(BigDecimal.valueOf(100))));
        when(planRepository.findByCargaisonId(1L)).thenReturn(Optional.empty());
        when(planRepository.save(any(PlanPaiement.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDto(any())).thenReturn(mock(PlanPaiementDto.class));

        service.create(req);

        verify(planRepository).save(argThat(p -> p.getEcheances().size() == 2));
    }
}
