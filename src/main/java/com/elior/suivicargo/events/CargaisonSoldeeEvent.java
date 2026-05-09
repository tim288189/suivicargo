package com.elior.suivicargo.events;

/**
 * Évènement émis quand le solde restant d'une cargaison atteint zéro.
 * Déclenche la génération + l'envoi de la facture.
 */
public record CargaisonSoldeeEvent(Long cargaisonId) {}
