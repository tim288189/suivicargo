package com.elior.suivicargo.models;

import com.elior.suivicargo.enums.StatutCargaison;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(
        name = "historique_statut",
        indexes = {
                @Index(name = "idx_histo_cargaison", columnList = "cargaison_id")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriqueStatut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cargaison_id", nullable = false, foreignKey = @ForeignKey(name = "fk_histo_cargaison"))
    private Cargaison cargaison;

    @Enumerated(EnumType.STRING)
    @Column(name = "ancien_statut", length = 30)
    private StatutCargaison ancienStatut;

    @Enumerated(EnumType.STRING)
    @Column(name = "nouveau_statut", nullable = false, length = 30)
    private StatutCargaison nouveauStatut;

    @Column(name = "commentaire", length = 500)
    private String commentaire;

    @Column(name = "auteur", length = 100)
    private String auteur;

    @CreatedDate
    @Column(name = "date_changement", nullable = false, updatable = false)
    private Instant dateChangement;
}
