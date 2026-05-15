package com.elior.suivicargo.models;

import com.elior.suivicargo.enums.StatutVoyage;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@Entity
@SQLRestriction("supprime = false")
@Table(
        name = "voyage",
        indexes = {
                @Index(name = "idx_voyage_navire", columnList = "navire_id"),
                @Index(name = "idx_voyage_statut", columnList = "statut")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voyage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "navire_id", nullable = false, foreignKey = @ForeignKey(name = "fk_voyage_navire"))
    private Navire navire;

    @Column(name = "port_depart", nullable = false, length = 100)
    private String portDepart;

    @Column(name = "port_arrivee", nullable = false, length = 100)
    private String portArrivee;

    @Column(name = "date_depart", nullable = false)
    private LocalDate dateDepart;

    @Column(name = "eta_arrivee", nullable = false)
    private LocalDate etaArrivee;

    @Column(name = "date_arrivee_reelle")
    private LocalDate dateArriveeReelle;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutVoyage statut = StatutVoyage.PROGRAMME;
}
