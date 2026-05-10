package com.elior.suivicargo.mappers;

import com.elior.suivicargo.dtos.CargaisonDetailDto;
import com.elior.suivicargo.dtos.CargaisonDto;
import com.elior.suivicargo.dtos.EvenementHistoriqueDto;
import com.elior.suivicargo.models.Cargaison;
import com.elior.suivicargo.models.Client;
import com.elior.suivicargo.models.HistoriqueStatut;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper
public interface CargaisonMapper {

    @Mapping(target = "clientId",          source = "client.id")
    @Mapping(target = "clientNomComplet",  source = "client",      qualifiedByName = "nomComplet")
    @Mapping(target = "clientTelephone",   source = "client.telephone")
    @Mapping(target = "conteneurId",       source = "conteneur.id")
    @Mapping(target = "conteneurNumero",   source = "conteneur.numero")
    @Mapping(target = "voyageId",          source = "voyage.id")
    @Mapping(target = "voyageNavireNom",   source = "voyage.navire.nom")
    @Mapping(target = "voyagePortDepart",  source = "voyage.portDepart")
    @Mapping(target = "voyagePortArrivee", source = "voyage.portArrivee")
    @Mapping(target = "montantRestant",    expression = "java(c.getMontantRestant())")
    CargaisonDto toDto(Cargaison c);

    @Mapping(target = "id",                   source = "cargaison.id")
    @Mapping(target = "numeroTracage",        source = "cargaison.numeroTracage")
    @Mapping(target = "clientId",             source = "cargaison.client.id")
    @Mapping(target = "clientNom",            source = "cargaison.client.nom")
    @Mapping(target = "clientPrenom",         source = "cargaison.client.prenom")
    @Mapping(target = "clientTelephone",      source = "cargaison.client.telephone")
    @Mapping(target = "clientEmail",          source = "cargaison.client.email")
    @Mapping(target = "adresseEnlevement",    source = "cargaison.client.adresseEnlevement")
    @Mapping(target = "adresseLivraison",     source = "cargaison.client.adresseLivraison")
    @Mapping(target = "conteneurId",          source = "cargaison.conteneur.id")
    @Mapping(target = "conteneurNumero",      source = "cargaison.conteneur.numero")
    @Mapping(target = "voyageId",             source = "cargaison.voyage.id")
    @Mapping(target = "voyageNavireId",       source = "cargaison.voyage.navire.id")
    @Mapping(target = "voyageNavireNom",      source = "cargaison.voyage.navire.nom")
    @Mapping(target = "voyagePortDepart",     source = "cargaison.voyage.portDepart")
    @Mapping(target = "voyagePortArrivee",    source = "cargaison.voyage.portArrivee")
    @Mapping(target = "voyageDateDepart",     source = "cargaison.voyage.dateDepart")
    @Mapping(target = "voyageEtaArrivee",     source = "cargaison.voyage.etaArrivee")
    @Mapping(target = "voyageStatut",         source = "cargaison.voyage.statut")
    @Mapping(target = "nombreColis",          source = "cargaison.nombreColis")
    @Mapping(target = "poidsKg",              source = "cargaison.poidsKg")
    @Mapping(target = "volumeM3",             source = "cargaison.volumeM3")
    @Mapping(target = "montantTotal",         source = "cargaison.montantTotal")
    @Mapping(target = "montantRegle",         source = "cargaison.montantRegle")
    @Mapping(target = "montantRestant",       expression = "java(cargaison.getMontantRestant())")
    @Mapping(target = "devise",               source = "cargaison.devise")
    @Mapping(target = "statut",               source = "cargaison.statut")
    @Mapping(target = "observations",         source = "cargaison.observations")
    @Mapping(target = "factureEnvoyee",       source = "cargaison.factureEnvoyee")
    @Mapping(target = "dateEnlevement",       source = "cargaison.dateEnlevement")
    @Mapping(target = "dateLivraisonEstimee", source = "cargaison.dateLivraisonEstimee")
    @Mapping(target = "dateLivraisonReelle",  source = "cargaison.dateLivraisonReelle")
    @Mapping(target = "dateCreation",         source = "cargaison.dateCreation")
    @Mapping(target = "historique",           source = "historique")
    CargaisonDetailDto toDetailDto(Cargaison cargaison, List<EvenementHistoriqueDto> historique);

    EvenementHistoriqueDto toHistoriqueDto(HistoriqueStatut h);

    List<EvenementHistoriqueDto> toHistoriqueDtos(List<HistoriqueStatut> historique);

    @Named("nomComplet")
    default String nomComplet(Client c) {
        if (c == null) return null;
        return (c.getPrenom() == null ? "" : c.getPrenom()) + " " + (c.getNom() == null ? "" : c.getNom());
    }
}
