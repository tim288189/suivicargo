package com.elior.suivicargo.models;

import com.elior.suivicargo.enums.ModePaiement;
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
        name = "reglement",
        indexes = {
                @Index(name = "idx_reglement_plan", columnList = "plan_paiement_id"),
                @Index(name = "idx_reglement_echeance", columnList = "echeance_id"),
                @Index(name = "idx_reglement_date", columnList = "date_reglement")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reglement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_paiement_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reglement_plan"))
    private PlanPaiement planPaiement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "echeance_id", foreignKey = @ForeignKey(name = "fk_reglement_echeance"))
    private Echeance echeance;

    @Column(name = "montant", nullable = false, precision = 14, scale = 2)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_paiement", nullable = false, length = 20)
    private ModePaiement modePaiement;

    @Column(name = "reference_transaction", length = 100)
    private String referenceTransaction;

    @Column(name = "date_reglement", nullable = false)
    private LocalDate dateReglement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encaisse_par_user_id", foreignKey = @ForeignKey(name = "fk_reglement_user"))
    private User encaissePar;

    @Column(name = "commentaire", length = 500)
    private String commentaire;
}
