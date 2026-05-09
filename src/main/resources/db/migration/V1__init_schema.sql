-- =====================================================================
-- V1 — Schéma initial : utilisateurs, clients, navires, voyages,
--      conteneurs, cargaisons, plans de paiement, règlements, factures
-- =====================================================================

-- ---------- Table app_user ----------
CREATE TABLE app_user (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    date_creation       DATETIME(6)  NOT NULL,
    date_modification   DATETIME(6),
    cree_par            VARCHAR(100),
    modifie_par         VARCHAR(100),
    version             BIGINT       NOT NULL DEFAULT 0,
    supprime            BOOLEAN      NOT NULL DEFAULT FALSE,

    email               VARCHAR(150) NOT NULL,
    password_hash       VARCHAR(255) NOT NULL,
    nom                 VARCHAR(100) NOT NULL,
    prenom              VARCHAR(100) NOT NULL,
    telephone           VARCHAR(30),
    role                VARCHAR(20)  NOT NULL,
    actif               BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_app_user      PRIMARY KEY (id),
    CONSTRAINT uk_user_email    UNIQUE (email)
) ENGINE=InnoDB;
CREATE INDEX idx_user_role ON app_user(role);

-- ---------- Table client ----------
CREATE TABLE client (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    date_creation       DATETIME(6)  NOT NULL,
    date_modification   DATETIME(6),
    cree_par            VARCHAR(100),
    modifie_par         VARCHAR(100),
    version             BIGINT       NOT NULL DEFAULT 0,
    supprime            BOOLEAN      NOT NULL DEFAULT FALSE,

    nom                 VARCHAR(100) NOT NULL,
    prenom              VARCHAR(100) NOT NULL,
    telephone           VARCHAR(30)  NOT NULL,
    email               VARCHAR(150),
    adresse_enlevement  VARCHAR(500) NOT NULL,
    adresse_livraison   VARCHAR(500) NOT NULL,

    CONSTRAINT pk_client PRIMARY KEY (id)
) ENGINE=InnoDB;
CREATE INDEX idx_client_telephone ON client(telephone);
CREATE INDEX idx_client_nom       ON client(nom);

-- ---------- Table navire ----------
CREATE TABLE navire (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    date_creation       DATETIME(6)  NOT NULL,
    date_modification   DATETIME(6),
    cree_par            VARCHAR(100),
    modifie_par         VARCHAR(100),
    version             BIGINT       NOT NULL DEFAULT 0,
    supprime            BOOLEAN      NOT NULL DEFAULT FALSE,

    nom                 VARCHAR(150) NOT NULL,
    imo                 VARCHAR(7)   NOT NULL,
    pavillon            VARCHAR(50),
    capacite_evp        INT,

    CONSTRAINT pk_navire     PRIMARY KEY (id),
    CONSTRAINT uk_navire_imo UNIQUE (imo)
) ENGINE=InnoDB;

-- ---------- Table voyage ----------
CREATE TABLE voyage (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    date_creation       DATETIME(6)  NOT NULL,
    date_modification   DATETIME(6),
    cree_par            VARCHAR(100),
    modifie_par         VARCHAR(100),
    version             BIGINT       NOT NULL DEFAULT 0,
    supprime            BOOLEAN      NOT NULL DEFAULT FALSE,

    navire_id           BIGINT       NOT NULL,
    port_depart         VARCHAR(100) NOT NULL,
    port_arrivee        VARCHAR(100) NOT NULL,
    date_depart         DATE         NOT NULL,
    eta_arrivee         DATE         NOT NULL,
    date_arrivee_reelle DATE,
    statut              VARCHAR(20)  NOT NULL,

    CONSTRAINT pk_voyage        PRIMARY KEY (id),
    CONSTRAINT fk_voyage_navire FOREIGN KEY (navire_id) REFERENCES navire(id)
) ENGINE=InnoDB;
CREATE INDEX idx_voyage_navire ON voyage(navire_id);
CREATE INDEX idx_voyage_statut ON voyage(statut);

-- ---------- Table conteneur ----------
CREATE TABLE conteneur (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    date_creation       DATETIME(6)  NOT NULL,
    date_modification   DATETIME(6),
    cree_par            VARCHAR(100),
    modifie_par         VARCHAR(100),
    version             BIGINT       NOT NULL DEFAULT 0,
    supprime            BOOLEAN      NOT NULL DEFAULT FALSE,

    numero              VARCHAR(11)  NOT NULL,
    type_conteneur      VARCHAR(20)  NOT NULL,
    voyage_id           BIGINT,

    CONSTRAINT pk_conteneur         PRIMARY KEY (id),
    CONSTRAINT uk_conteneur_numero  UNIQUE (numero),
    CONSTRAINT fk_conteneur_voyage  FOREIGN KEY (voyage_id) REFERENCES voyage(id)
) ENGINE=InnoDB;
CREATE INDEX idx_conteneur_voyage ON conteneur(voyage_id);

-- ---------- Table cargaison ----------
CREATE TABLE cargaison (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    date_creation       DATETIME(6)     NOT NULL,
    date_modification   DATETIME(6),
    cree_par            VARCHAR(100),
    modifie_par         VARCHAR(100),
    version             BIGINT          NOT NULL DEFAULT 0,
    supprime            BOOLEAN         NOT NULL DEFAULT FALSE,

    numero_tracage      VARCHAR(30)     NOT NULL,
    client_id           BIGINT          NOT NULL,
    conteneur_id        BIGINT,
    nombre_colis        INT             NOT NULL,
    poids_kg            DECIMAL(12,3),
    volume_m3           DECIMAL(12,3),
    montant_total       DECIMAL(14,2)   NOT NULL,
    montant_regle       DECIMAL(14,2)   NOT NULL DEFAULT 0,
    devise              VARCHAR(3)      NOT NULL DEFAULT 'XOF',
    statut              VARCHAR(30)     NOT NULL,
    observations        VARCHAR(1000),
    facture_envoyee     BOOLEAN         NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_cargaison           PRIMARY KEY (id),
    CONSTRAINT uk_cargaison_tracage   UNIQUE (numero_tracage),
    CONSTRAINT fk_cargaison_client    FOREIGN KEY (client_id)    REFERENCES client(id),
    CONSTRAINT fk_cargaison_conteneur FOREIGN KEY (conteneur_id) REFERENCES conteneur(id)
) ENGINE=InnoDB;
CREATE INDEX idx_cargaison_client    ON cargaison(client_id);
CREATE INDEX idx_cargaison_statut    ON cargaison(statut);
CREATE INDEX idx_cargaison_conteneur ON cargaison(conteneur_id);

-- ---------- Table historique_statut ----------
CREATE TABLE historique_statut (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    cargaison_id        BIGINT       NOT NULL,
    ancien_statut       VARCHAR(30),
    nouveau_statut      VARCHAR(30)  NOT NULL,
    commentaire         VARCHAR(500),
    auteur              VARCHAR(100),
    date_changement     DATETIME(6)  NOT NULL,

    CONSTRAINT pk_historique_statut PRIMARY KEY (id),
    CONSTRAINT fk_histo_cargaison   FOREIGN KEY (cargaison_id) REFERENCES cargaison(id)
) ENGINE=InnoDB;
CREATE INDEX idx_histo_cargaison ON historique_statut(cargaison_id);

-- ---------- Table plan_paiement ----------
CREATE TABLE plan_paiement (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    date_creation       DATETIME(6)  NOT NULL,
    date_modification   DATETIME(6),
    cree_par            VARCHAR(100),
    modifie_par         VARCHAR(100),
    version             BIGINT       NOT NULL DEFAULT 0,
    supprime            BOOLEAN      NOT NULL DEFAULT FALSE,

    cargaison_id        BIGINT       NOT NULL,
    montant_total       DECIMAL(14,2) NOT NULL,
    devise              VARCHAR(3)   NOT NULL DEFAULT 'XOF',
    statut              VARCHAR(20)  NOT NULL,

    CONSTRAINT pk_plan_paiement   PRIMARY KEY (id),
    CONSTRAINT uk_plan_cargaison  UNIQUE (cargaison_id),
    CONSTRAINT fk_plan_cargaison  FOREIGN KEY (cargaison_id) REFERENCES cargaison(id)
) ENGINE=InnoDB;

-- ---------- Table echeance ----------
CREATE TABLE echeance (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    date_creation       DATETIME(6)  NOT NULL,
    date_modification   DATETIME(6),
    cree_par            VARCHAR(100),
    modifie_par         VARCHAR(100),
    version             BIGINT       NOT NULL DEFAULT 0,
    supprime            BOOLEAN      NOT NULL DEFAULT FALSE,

    plan_paiement_id    BIGINT       NOT NULL,
    ordre               INT          NOT NULL,
    libelle             VARCHAR(100) NOT NULL,
    montant_prevu       DECIMAL(14,2) NOT NULL,
    date_echeance       DATE         NOT NULL,
    statut              VARCHAR(20)  NOT NULL,

    CONSTRAINT pk_echeance      PRIMARY KEY (id),
    CONSTRAINT fk_echeance_plan FOREIGN KEY (plan_paiement_id) REFERENCES plan_paiement(id)
) ENGINE=InnoDB;
CREATE INDEX idx_echeance_plan ON echeance(plan_paiement_id);
CREATE INDEX idx_echeance_date ON echeance(date_echeance);

-- ---------- Table reglement ----------
CREATE TABLE reglement (
    id                       BIGINT       NOT NULL AUTO_INCREMENT,
    date_creation            DATETIME(6)  NOT NULL,
    date_modification        DATETIME(6),
    cree_par                 VARCHAR(100),
    modifie_par              VARCHAR(100),
    version                  BIGINT       NOT NULL DEFAULT 0,
    supprime                 BOOLEAN      NOT NULL DEFAULT FALSE,

    plan_paiement_id         BIGINT       NOT NULL,
    echeance_id              BIGINT,
    montant                  DECIMAL(14,2) NOT NULL,
    mode_paiement            VARCHAR(20)  NOT NULL,
    reference_transaction    VARCHAR(100),
    date_reglement           DATE         NOT NULL,
    encaisse_par_user_id     BIGINT,
    commentaire              VARCHAR(500),

    CONSTRAINT pk_reglement          PRIMARY KEY (id),
    CONSTRAINT fk_reglement_plan     FOREIGN KEY (plan_paiement_id)    REFERENCES plan_paiement(id),
    CONSTRAINT fk_reglement_echeance FOREIGN KEY (echeance_id)         REFERENCES echeance(id),
    CONSTRAINT fk_reglement_user     FOREIGN KEY (encaisse_par_user_id) REFERENCES app_user(id)
) ENGINE=InnoDB;
CREATE INDEX idx_reglement_plan     ON reglement(plan_paiement_id);
CREATE INDEX idx_reglement_echeance ON reglement(echeance_id);
CREATE INDEX idx_reglement_date     ON reglement(date_reglement);

-- ---------- Table facture ----------
CREATE TABLE facture (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    date_creation       DATETIME(6)  NOT NULL,
    date_modification   DATETIME(6),
    cree_par            VARCHAR(100),
    modifie_par         VARCHAR(100),
    version             BIGINT       NOT NULL DEFAULT 0,
    supprime            BOOLEAN      NOT NULL DEFAULT FALSE,

    numero              VARCHAR(30)  NOT NULL,
    cargaison_id        BIGINT       NOT NULL,
    date_facture        DATE         NOT NULL,
    montant_ht          DECIMAL(14,2) NOT NULL,
    montant_tva         DECIMAL(14,2) NOT NULL DEFAULT 0,
    montant_ttc         DECIMAL(14,2) NOT NULL,
    devise              VARCHAR(3)   NOT NULL DEFAULT 'XOF',
    chemin_pdf          VARCHAR(500),
    envoyee_email       BOOLEAN      DEFAULT FALSE,
    envoyee_whatsapp    BOOLEAN      DEFAULT FALSE,
    date_envoi          DATETIME(6),

    CONSTRAINT pk_facture            PRIMARY KEY (id),
    CONSTRAINT uk_facture_numero     UNIQUE (numero),
    CONSTRAINT uk_facture_cargaison  UNIQUE (cargaison_id),
    CONSTRAINT fk_facture_cargaison  FOREIGN KEY (cargaison_id) REFERENCES cargaison(id)
) ENGINE=InnoDB;
