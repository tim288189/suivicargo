package com.elior.suivicargo.models;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Champs communs à toutes les entités persistées : id, audit, version optimiste, soft-delete.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "date_creation", nullable = false, updatable = false)
    private Instant dateCreation;

    @LastModifiedDate
    @Column(name = "date_modification")
    private Instant dateModification;

    @CreatedBy
    @Column(name = "cree_par", updatable = false, length = 100)
    private String creePar;

    @LastModifiedBy
    @Column(name = "modifie_par", length = 100)
    private String modifiePar;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "supprime", nullable = false)
    private boolean supprime = false;
}
