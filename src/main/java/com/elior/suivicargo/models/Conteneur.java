package com.elior.suivicargo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "conteneur",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_conteneur_numero", columnNames = "numero")
        },
        indexes = {
                @Index(name = "idx_conteneur_voyage", columnList = "voyage_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conteneur extends BaseEntity {

    /** Numéro ISO 6346 : 4 lettres + 7 chiffres (ex: MSCU1234567) */
    @Column(name = "numero", nullable = false, length = 11)
    private String numero;

    /** Type : 20', 40', 40HC, REEFER, etc. */
    @Column(name = "type_conteneur", nullable = false, length = 20)
    private String typeConteneur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voyage_id", foreignKey = @ForeignKey(name = "fk_conteneur_voyage"))
    private Voyage voyage;
}
