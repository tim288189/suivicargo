package com.elior.suivicargo.models;

import com.elior.suivicargo.enums.StatutPaiement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "plan_paiement",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_plan_cargaison", columnNames = "cargaison_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanPaiement extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cargaison_id", nullable = false, foreignKey = @ForeignKey(name = "fk_plan_cargaison"))
    private Cargaison cargaison;

    @Column(name = "montant_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal montantTotal;

    @Column(name = "devise", nullable = false, length = 3)
    private String devise = "XOF";

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutPaiement statut = StatutPaiement.EN_ATTENTE;

    @OneToMany(mappedBy = "planPaiement", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("ordre ASC")
    @Builder.Default
    private List<Echeance> echeances = new ArrayList<>();

    public void addEcheance(Echeance e) {
        echeances.add(e);
        e.setPlanPaiement(this);
    }
}
