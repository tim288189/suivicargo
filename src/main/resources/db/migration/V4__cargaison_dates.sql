-- =====================================================================
-- V4 — Ajout des dates d'enlèvement, livraison estimée et livraison réelle
-- =====================================================================

ALTER TABLE cargaison
    ADD COLUMN date_enlevement         DATE,
    ADD COLUMN date_livraison_estimee  DATE,
    ADD COLUMN date_livraison_reelle   DATE;

-- Backfill pour les cargaisons existantes : enlèvement = jour de création (date_creation),
-- ETA = enlèvement + 30 jours.
UPDATE cargaison
   SET date_enlevement        = DATE(date_creation),
       date_livraison_estimee = DATE(DATE_ADD(date_creation, INTERVAL 30 DAY))
 WHERE date_enlevement IS NULL;

-- Renseigne la date de livraison réelle pour les cargaisons déjà LIVRE
UPDATE cargaison
   SET date_livraison_reelle = DATE(date_modification)
 WHERE statut = 'LIVRE'
   AND date_livraison_reelle IS NULL;

ALTER TABLE cargaison
    MODIFY COLUMN date_enlevement        DATE NOT NULL,
    MODIFY COLUMN date_livraison_estimee DATE NOT NULL;

CREATE INDEX idx_cargaison_eta ON cargaison(date_livraison_estimee);
