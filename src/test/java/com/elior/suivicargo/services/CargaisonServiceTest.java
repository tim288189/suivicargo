package com.elior.suivicargo.services;

import com.elior.suivicargo.dtos.CargaisonDto;
import com.elior.suivicargo.dtos.CreateCargaisonRequest;
import com.elior.suivicargo.enums.StatutCargaison;
import com.elior.suivicargo.exceptions.BusinessException;
import com.elior.suivicargo.mappers.CargaisonMapper;
import com.elior.suivicargo.models.Cargaison;
import com.elior.suivicargo.models.Client;
import com.elior.suivicargo.models.HistoriqueStatut;
import com.elior.suivicargo.repositories.CargaisonRepository;
import com.elior.suivicargo.repositories.ClientRepository;
import com.elior.suivicargo.repositories.HistoriqueStatutRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargaisonServiceTest {

    @Mock private CargaisonRepository cargaisonRepository;
    @Mock private HistoriqueStatutRepository historiqueRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private NumeroTracageService numeroTracageService;
    @Mock private CargaisonMapper mapper;

    @InjectMocks private CargaisonService service;

    @Test
    @DisplayName("create() refuse si client introuvable")
    void create_clientNotFound() {
        CreateCargaisonRequest req = new CreateCargaisonRequest(
                99L, 1, null, null, BigDecimal.TEN, BigDecimal.ZERO, "XOF", null);
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Client introuvable");
    }

    @Test
    @DisplayName("create() refuse si montantRegle dépasse montantTotal")
    void create_montantTropEleve() {
        CreateCargaisonRequest req = new CreateCargaisonRequest(
                1L, 1, null, null, BigDecimal.valueOf(100), BigDecimal.valueOf(200), "XOF", null);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(new Client()));

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("montant réglé ne peut pas dépasser");
    }

    @Test
    @DisplayName("create() ok : génère numéro, sauve cargaison + historique, défaut statut ENLEVE")
    void create_ok() {
        CreateCargaisonRequest req = new CreateCargaisonRequest(
                1L, 3, null, null, BigDecimal.valueOf(1000), BigDecimal.valueOf(500), "XOF", "obs");

        Client client = new Client();
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(numeroTracageService.genererNumero()).thenReturn("MAR-2026-000001");
        when(cargaisonRepository.save(any(Cargaison.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDto(any(Cargaison.class))).thenReturn(
                new CargaisonDto(1L, "MAR-2026-000001", null, null, null, null, null,
                        3, null, null, BigDecimal.valueOf(1000), BigDecimal.valueOf(500),
                        BigDecimal.valueOf(500), "XOF", StatutCargaison.ENLEVE, null, false,
                        null, null, null, null));

        service.create(req);

        verify(cargaisonRepository).save(argThat(c ->
                c.getNumeroTracage().equals("MAR-2026-000001")
                && c.getStatut() == StatutCargaison.ENLEVE
                && c.getNombreColis() == 3
        ));
        verify(historiqueRepository).save(any(HistoriqueStatut.class));
    }

    @Test
    @DisplayName("changerStatut() ne fait rien si statut identique")
    void changerStatut_identique() {
        Cargaison c = new Cargaison();
        c.setStatut(StatutCargaison.EN_MER);
        when(cargaisonRepository.findById(1L)).thenReturn(Optional.of(c));
        when(mapper.toDto(c)).thenReturn(mock(CargaisonDto.class));

        service.changerStatut(1L, StatutCargaison.EN_MER, "ignored");

        verify(historiqueRepository, never()).save(any());
        verify(cargaisonRepository, never()).save(any());
    }

    @Test
    @DisplayName("changerStatut() crée une entrée d'historique avec ancien et nouveau statut")
    void changerStatut_ok() {
        Cargaison c = new Cargaison();
        c.setStatut(StatutCargaison.ENLEVE);
        when(cargaisonRepository.findById(1L)).thenReturn(Optional.of(c));
        when(cargaisonRepository.save(c)).thenReturn(c);
        when(mapper.toDto(c)).thenReturn(mock(CargaisonDto.class));

        service.changerStatut(1L, StatutCargaison.EN_MER, "départ");

        verify(historiqueRepository).save(argThat(h ->
                h.getAncienStatut() == StatutCargaison.ENLEVE
                && h.getNouveauStatut() == StatutCargaison.EN_MER
                && "départ".equals(h.getCommentaire())
        ));
    }

    @Test
    @DisplayName("trackPublic() retourne 404 si numéro inconnu")
    void trackPublic_notFound() {
        when(cargaisonRepository.findByNumeroTracage("BAD")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.trackPublic("BAD"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Aucune cargaison");
    }
}
