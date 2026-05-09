package com.elior.suivicargo.services;

import com.elior.suivicargo.dtos.CreateReglementRequest;
import com.elior.suivicargo.dtos.ReglementDto;
import com.elior.suivicargo.enums.ModePaiement;
import com.elior.suivicargo.events.CargaisonSoldeeEvent;
import com.elior.suivicargo.exceptions.BusinessException;
import com.elior.suivicargo.mappers.ReglementMapper;
import com.elior.suivicargo.models.Cargaison;
import com.elior.suivicargo.models.Reglement;
import com.elior.suivicargo.repositories.CargaisonRepository;
import com.elior.suivicargo.repositories.EcheanceRepository;
import com.elior.suivicargo.repositories.PlanPaiementRepository;
import com.elior.suivicargo.repositories.ReglementRepository;
import com.elior.suivicargo.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReglementServiceTest {

    @Mock private ReglementRepository reglementRepository;
    @Mock private PlanPaiementRepository planRepository;
    @Mock private EcheanceRepository echeanceRepository;
    @Mock private CargaisonRepository cargaisonRepository;
    @Mock private UserRepository userRepository;
    @Mock private ReglementMapper mapper;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private ReglementService service;

    private CreateReglementRequest req(BigDecimal montant) {
        return new CreateReglementRequest(
                1L, montant, ModePaiement.ESPECES, "REF",
                LocalDate.now(), null, null);
    }

    private Cargaison cargaison(BigDecimal total, BigDecimal regle) {
        Cargaison c = new Cargaison();
        c.setId(1L);          // même id que celui demandé via req(...) pour cohérence des stubs
        c.setMontantTotal(total);
        c.setMontantRegle(regle);
        c.setNumeroTracage("MAR-2026-000001");
        return c;
    }

    @Test
    @DisplayName("Refuse si la cargaison n'existe pas")
    void enregistrer_cargaisonNotFound() {
        when(cargaisonRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.enregistrer(req(BigDecimal.TEN)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Refuse si nouveau total dépasse le montant total cargaison")
    void enregistrer_depasseTotal() {
        Cargaison c = cargaison(BigDecimal.valueOf(100), BigDecimal.valueOf(80));
        when(cargaisonRepository.findById(1L)).thenReturn(Optional.of(c));

        assertThatThrownBy(() -> service.enregistrer(req(BigDecimal.valueOf(50))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("dépasserait");
    }

    @Test
    @DisplayName("Cargaison non soldée : pas d'événement publié")
    void enregistrer_partiel_pasEvent() {
        Cargaison c = cargaison(BigDecimal.valueOf(100), BigDecimal.valueOf(20));
        when(cargaisonRepository.findById(1L)).thenReturn(Optional.of(c));
        when(planRepository.findByCargaisonId(1L)).thenReturn(Optional.empty());
        when(reglementRepository.save(any(Reglement.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDto(any())).thenReturn(mock(ReglementDto.class));

        service.enregistrer(req(BigDecimal.valueOf(30)));

        assertThat(c.getMontantRegle()).isEqualByComparingTo(BigDecimal.valueOf(50));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Cargaison soldée par ce règlement : événement CargaisonSoldee publié")
    void enregistrer_solde_publieEvent() {
        Cargaison c = cargaison(BigDecimal.valueOf(100), BigDecimal.valueOf(70));
        when(cargaisonRepository.findById(1L)).thenReturn(Optional.of(c));
        when(planRepository.findByCargaisonId(1L)).thenReturn(Optional.empty());
        when(reglementRepository.save(any(Reglement.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDto(any())).thenReturn(mock(ReglementDto.class));

        service.enregistrer(req(BigDecimal.valueOf(30)));

        assertThat(c.getMontantRegle()).isEqualByComparingTo(BigDecimal.valueOf(100));

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue())
                .isInstanceOfSatisfying(CargaisonSoldeeEvent.class,
                        ev -> assertThat(ev.cargaisonId()).isEqualTo(1L));
    }

    @Test
    @DisplayName("Si facture déjà envoyée, ne republie pas l'événement")
    void enregistrer_factureDejaEnvoyee_pasEvent() {
        Cargaison c = cargaison(BigDecimal.valueOf(100), BigDecimal.valueOf(70));
        c.setFactureEnvoyee(true);
        when(cargaisonRepository.findById(1L)).thenReturn(Optional.of(c));
        when(planRepository.findByCargaisonId(1L)).thenReturn(Optional.empty());
        when(reglementRepository.save(any(Reglement.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDto(any())).thenReturn(mock(ReglementDto.class));

        service.enregistrer(req(BigDecimal.valueOf(30)));

        verify(eventPublisher, never()).publishEvent(any());
    }
}
