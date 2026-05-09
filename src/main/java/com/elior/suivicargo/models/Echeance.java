package com.elior.suivicargo.models;

import com.elior.suivicargo.enums.StatutPaiement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "echeance",
        indexes = {
                @Index(name = "idx_echeance_plan", columnList = "plan_paiement_id"),
                @Index(name = "idx_echeance_date", columnList = "date_echeance")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Echeance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_paiement_id", nullable = false, foreignKey = @ForeignKey(name = "fk_echeance_plan"))
    private PlanPaiement planPaiement;

    @Column(name = "ordre", nullable = false)
    private Integer ordre;

    @Column(name = "libelle", nullable = false, length = 100)
    private String libelle;

    @Column(name = "montant_prevu", nullable = false, precision = 14, scale = 2)
    private BigDecimal montantPrevu;

    @Column(name = "date_echeance", nullable = false)
    private LocalDate dateEcheance;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutPaiement statut = StatutPaiement.EN_ATTENTE;
}
