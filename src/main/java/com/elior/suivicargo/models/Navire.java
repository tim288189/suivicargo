package com.elior.suivicargo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "navire",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_navire_imo", columnNames = "imo")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Navire extends BaseEntity {

    @Column(name = "nom", nullable = false, length = 150)
    private String nom;

    /** Numéro IMO international, 7 chiffres */
    @Column(name = "imo", nullable = false, length = 7)
    private String imo;

    @Column(name = "pavillon", length = 50)
    private String pavillon;

    /** Capacité en EVP (équivalent vingt pieds, TEU) */
    @Column(name = "capacite_evp")
    private Integer capaciteEvp;
}
