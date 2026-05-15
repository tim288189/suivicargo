package com.elior.suivicargo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@Entity
@SQLRestriction("supprime = false")
@Table(
        name = "client",
        indexes = {
                @Index(name = "idx_client_telephone", columnList = "telephone"),
                @Index(name = "idx_client_nom", columnList = "nom")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client extends BaseEntity {

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "telephone", nullable = false, length = 30)
    private String telephone;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "adresse_enlevement", nullable = false, length = 500)
    private String adresseEnlevement;

    @Column(name = "adresse_livraison", nullable = false, length = 500)
    private String adresseLivraison;
}
