package com.elior.suivicargo.services;

import com.elior.suivicargo.dtos.FactureDto;
import com.elior.suivicargo.exceptions.BusinessException;
import com.elior.suivicargo.mappers.FactureMapper;
import com.elior.suivicargo.models.Cargaison;
import com.elior.suivicargo.models.Client;
import com.elior.suivicargo.models.Facture;
import com.elior.suivicargo.repositories.CargaisonRepository;
import com.elior.suivicargo.repositories.FactureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FactureServiceTest {

    @Mock private FactureRepository factureRepository;
    @Mock private CargaisonRepository cargaisonRepository;
    @Mock private TemplateService templateService;
    @Mock private PdfService pdfService;
    @Mock private FactureMapper mapper;

    @InjectMocks private FactureService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "prefix", "FAC");
    }

    private Cargaison cargaison() {
        Client cli = new Client();
        cli.setNom("Doe");
        cli.setPrenom("John");
        Cargaison c = new Cargaison();
        c.setId(10L);
        c.setNumeroTracage("MAR-2026-000001");
        c.setMontantTotal(BigDecimal.valueOf(1000));
        c.setDevise("XOF");
        c.setNombreColis(2);
        c.setClient(cli);
        return c;
    }

    @Test
    @DisplayName("genererOuObtenir() crée la facture si absente")
    void genererOuObtenir_creeSiAbsente() {
        Cargaison c = cargaison();
        when(factureRepository.findByCargaisonId(10L)).thenReturn(Optional.empty());
        when(cargaisonRepository.findById(10L)).thenReturn(Optional.of(c));
        when(factureRepository.countByAnnee(LocalDate.now().getYear())).thenReturn(0L);
        when(factureRepository.findByNumero(any())).thenReturn(Optional.empty());
        when(factureRepository.save(any(Facture.class))).thenAnswer(inv -> inv.getArgument(0));

        Facture f = service.genererOuObtenir(10L);

        assertThat(f.getNumero()).matches("^FAC-\\d{4}-\\d{6}$");
        assertThat(f.getMontantHt()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(f.getMontantTtc()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        verify(factureRepository).save(any(Facture.class));
    }

    @Test
    @DisplayName("genererOuObtenir() retourne la facture existante sans en recréer")
    void genererOuObtenir_retourneExistante() {
        Facture existante = Facture.builder().numero("FAC-2026-000001").build();
        when(factureRepository.findByCargaisonId(10L)).thenReturn(Optional.of(existante));

        Facture f = service.genererOuObtenir(10L);

        assertThat(f).isSameAs(existante);
        verify(factureRepository, never()).save(any());
    }

    @Test
    @DisplayName("genererPdf() : 404 si pas de facture")
    void genererPdf_factureNotFound() {
        when(factureRepository.findByCargaisonId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.genererPdf(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Aucune facture");
    }

    @Test
    @DisplayName("renderPdf() rend le template puis convertit en PDF")
    void renderPdf_ok() {
        Facture f = Facture.builder()
                .numero("FAC-2026-000001")
                .dateFacture(LocalDate.now())
                .montantHt(BigDecimal.TEN)
                .montantTva(BigDecimal.ZERO)
                .montantTtc(BigDecimal.TEN)
                .devise("XOF")
                .cargaison(cargaison())
                .build();
        when(templateService.render(eq("facture.ftl"), any())).thenReturn("<html>x</html>");
        when(pdfService.htmlToPdf("<html>x</html>")).thenReturn(new byte[]{1, 2, 3});

        byte[] pdf = service.renderPdf(f);

        assertThat(pdf).containsExactly(1, 2, 3);
        verify(templateService).render(eq("facture.ftl"), any());
        verify(pdfService).htmlToPdf("<html>x</html>");
    }

    @Test
    @DisplayName("marquerEnvoyeeEmail() positionne flag + dateEnvoi")
    void marquerEnvoyeeEmail_ok() {
        Facture f = Facture.builder().build();
        when(factureRepository.findById(1L)).thenReturn(Optional.of(f));

        Instant date = Instant.now();
        service.marquerEnvoyeeEmail(1L, date);

        assertThat(f.isEnvoyeeEmail()).isTrue();
        assertThat(f.getDateEnvoi()).isEqualTo(date);
        verify(factureRepository).save(f);
    }

    @Test
    @DisplayName("getByCargaisonId() délègue au repository et au mapper")
    void getByCargaisonId_ok() {
        Facture f = Facture.builder().build();
        when(factureRepository.findByCargaisonId(10L)).thenReturn(Optional.of(f));
        when(mapper.toDto(f)).thenReturn(mock(FactureDto.class));

        service.getByCargaisonId(10L);

        verify(mapper).toDto(f);
    }
}
