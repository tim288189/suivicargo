package com.elior.suivicargo.enums;

/**
 * Rôles applicatifs.
 * <ul>
 *   <li>ADMIN — gestion des utilisateurs (création/suppression employés)</li>
 *   <li>SUPERVISOR — gestion opérationnelle (navires, voyages, plans de paiement)</li>
 *   <li>EMPLOYEE — exécution terrain (enlèvement, encaissement)</li>
 * </ul>
 */
public enum Role {
    ADMIN,
    SUPERVISOR,
    EMPLOYEE
}
