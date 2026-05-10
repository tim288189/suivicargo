-- =====================================================================
-- V5 — Rattachement direct cargaison ↔ voyage
-- Une cargaison peut être affectée à un voyage (un navire + un trajet daté).
-- Plusieurs cargaisons peuvent être affectées au même voyage.
-- =====================================================================

ALTER TABLE cargaison
    ADD COLUMN voyage_id BIGINT NULL,
    ADD CONSTRAINT fk_cargaison_voyage
        FOREIGN KEY (voyage_id) REFERENCES voyage(id);

CREATE INDEX idx_cargaison_voyage ON cargaison(voyage_id);

-- Backfill : si certaines cargaisons étaient rattachées via un conteneur lié à un voyage,
-- on remonte le voyage_id directement sur la cargaison.
UPDATE cargaison c
   JOIN conteneur ct ON ct.id = c.conteneur_id
   SET c.voyage_id = ct.voyage_id
 WHERE c.voyage_id IS NULL
   AND ct.voyage_id IS NOT NULL;
