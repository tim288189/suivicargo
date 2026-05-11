package com.elior.suivicargo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@SQLRestriction("supprime = false")
@Table(
        name = "facture",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_facture_numero", columnNames = "numero"),
                @UniqueConstraint(name = "uk_facture_cargaison", columnNames = "cargaison_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Facture extends BaseEntity {

    @Column(name = "numero", nullable = false, length = 30)
    private String numero;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cargaison_id", nullable = false, foreignKey = @ForeignKey(name = "fk_facture_cargaison"))
    private Cargaison cargaison;

    @Column(name = "date_facture", nullable = false)
    private LocalDate dateFacture;

    @Column(name = "montant_ht", nullable = false, precision = 14, scale = 2)
    private BigDecimal montantHt;

    @Column(name = "montant_tva", nullable = false, precision = 14, scale = 2)
    private BigDecimal montantTva = BigDecimal.ZERO;

    @Column(name = "montant_ttc", nullable = false, precision = 14, scale = 2)
    private BigDecimal montantTtc;

    @Column(name = "devise", nullable = false, length = 3)
    private String devise = "XOF";

    @Column(name = "chemin_pdf", length = 500)
    private String cheminPdf;

    @Column(name = "envoyee_email")
    private boolean envoyeeEmail = false;

    @Column(name = "envoyee_whatsapp")
    private boolean envoyeeWhatsapp = false;

    @Column(name = "date_envoi")
    private Instant dateEnvoi;
}
