-- =====================================================================
-- V2 — Création de l'utilisateur admin par défaut
--
-- Email    : admin@suivicargo.local
-- Password : admin123  (À CHANGER après le premier login)
--
-- Hash BCrypt généré pour 'admin123' :
--   $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- =====================================================================

INSERT INTO app_user (
    date_creation, version, supprime,
    email, password_hash, nom, prenom, role, actif
) VALUES (
    CURRENT_TIMESTAMP(6), 0, FALSE,
    'admin@suivicargo.local',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Admin',
    'Initial',
    'ADMIN',
    TRUE
);
