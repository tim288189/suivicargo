package com.elior.suivicargo.models;

import com.elior.suivicargo.enums.StatutCargaison;
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
        name = "cargaison",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cargaison_tracage", columnNames = "numero_tracage")
        },
        indexes = {
                @Index(name = "idx_cargaison_client", columnList = "client_id"),
                @Index(name = "idx_cargaison_statut", columnList = "statut"),
                @Index(name = "idx_cargaison_conteneur", columnList = "conteneur_id"),
                @Index(name = "idx_cargaison_eta", columnList = "date_livraison_estimee")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cargaison extends BaseEntity {

    /** Format MAR-YYYY-NNNNNN (ex: MAR-2026-000042). Unique. */
    @Column(name = "numero_tracage", nullable = false, length = 30)
    private String numeroTracage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cargaison_client"))
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conteneur_id", foreignKey = @ForeignKey(name = "fk_cargaison_conteneur"))
    private Conteneur conteneur;

    @Column(name = "nombre_colis", nullable = false)
    private Integer nombreColis;

    @Column(name = "poids_kg", precision = 12, scale = 3)
    private BigDecimal poidsKg;

    @Column(name = "volume_m3", precision = 12, scale = 3)
    private BigDecimal volumeM3;

    @Column(name = "montant_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal montantTotal;

    @Column(name = "montant_regle", nullable = false, precision = 14, scale = 2)
    private BigDecimal montantRegle = BigDecimal.ZERO;

    @Column(name = "devise", nullable = false, length = 3)
    private String devise = "XOF";

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 30)
    private StatutCargaison statut = StatutCargaison.ENLEVE;

    @Column(name = "observations", length = 1000)
    private String observations;

    @Column(name = "facture_envoyee", nullable = false)
    private boolean factureEnvoyee = false;

    /** Date à laquelle la cargaison a été enlevée chez le client. Par défaut = today. */
    @Column(name = "date_enlevement", nullable = false)
    private LocalDate dateEnlevement;

    /** Date estimée de livraison au destinataire. Par défaut = enlèvement + 1 mois. */
    @Column(name = "date_livraison_estimee", nullable = false)
    private LocalDate dateLivraisonEstimee;

    /** Date effective de livraison (renseignée quand statut = LIVRE). */
    @Column(name = "date_livraison_reelle")
    private LocalDate dateLivraisonReelle;

    @Transient
    public BigDecimal getMontantRestant() {
        BigDecimal regle = montantRegle != null ? montantRegle : BigDecimal.ZERO;
        BigDecimal total = montantTotal != null ? montantTotal : BigDecimal.ZERO;
        return total.subtract(regle);
    }

    @Transient
    public boolean isSolde() {
        return getMontantRestant().compareTo(BigDecimal.ZERO) <= 0;
    }
}
