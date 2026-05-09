-- =====================================================================
-- V3 — Correction du mot de passe admin
--
-- La migration V2 contenait un hash BCrypt invalide (qui ne correspondait
-- à aucun mot de passe). On le remplace par un hash valide pour 'admin123'.
--
-- Email    : admin@suivicargo.local
-- Password : admin123  (À CHANGER après le premier login)
-- =====================================================================

UPDATE app_user
   SET password_hash = '$2b$10$tK3NbKhuaudVLJqwRf.BWOkAwBRx7DWF77L9fbNQccGIlDl8/76oK'
 WHERE email = 'admin@suivicargo.local';
