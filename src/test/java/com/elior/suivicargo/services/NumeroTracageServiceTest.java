package com.elior.suivicargo.services;

import com.elior.suivicargo.repositories.CargaisonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NumeroTracageServiceTest {

    @Mock
    private CargaisonRepository repository;

    @InjectMocks
    private NumeroTracageService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "prefix", "MAR");
    }

    @Test
    @DisplayName("Génère le format PREFIX-YYYY-NNNNNN avec la séquence + 1")
    void genererNumero_ok() {
        when(repository.countByAnnee(LocalDate.now().getYear())).thenReturn(41L);
        when(repository.existsByNumeroTracage(anyString())).thenReturn(false);

        String numero = service.genererNumero();

        assertThat(numero).matches("^MAR-\\d{4}-\\d{6}$");
        assertThat(numero).endsWith("-000042"); // 41 + 1
    }

    @Test
    @DisplayName("Si collision, incrémente jusqu'à trouver un numéro libre")
    void genererNumero_avecCollision() {
        int annee = LocalDate.now().getYear();
        when(repository.countByAnnee(annee)).thenReturn(0L);
        when(repository.existsByNumeroTracage(String.format("MAR-%d-000001", annee))).thenReturn(true);
        when(repository.existsByNumeroTracage(String.format("MAR-%d-000002", annee))).thenReturn(false);

        String numero = service.genererNumero();

        assertThat(numero).isEqualTo(String.format("MAR-%d-000002", annee));
    }
}
