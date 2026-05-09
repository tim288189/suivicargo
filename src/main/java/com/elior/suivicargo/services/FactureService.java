package com.elior.suivicargo.services;

import com.elior.suivicargo.dtos.FactureDto;
import com.elior.suivicargo.exceptions.BusinessException;
import com.elior.suivicargo.mappers.FactureMapper;
import com.elior.suivicargo.models.Cargaison;
import com.elior.suivicargo.models.Facture;
import com.elior.suivicargo.repositories.CargaisonRepository;
import com.elior.suivicargo.repositories.FactureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactureService {

    private final FactureRepository factureRepository;
    private final CargaisonRepository cargaisonRepository;
    private final TemplateService templateService;
    private final PdfService pdfService;
    private final FactureMapper mapper;

    @Value("${app.facture.prefix:FAC}")
    private String prefix;

    /**
     * Génère ou retourne la facture associée à une cargaison.
     * Numérotation séquentielle sans trou (obligation comptable).
     */
    @Transactional
    public Facture genererOuObtenir(Long cargaisonId) {
        return factureRepository.findByCargaisonId(cargaisonId)
                .orElseGet(() -> creer(cargaisonId));
    }

    @Transactional(readOnly = true)
    public FactureDto getByCargaisonId(Long cargaisonId) {
        Facture f = factureRepository.findByCargaisonId(cargaisonId)
                .orElseThrow(() -> BusinessException.notFound("FACTURE_NOT_FOUND",
                        "Aucune facture pour la cargaison " + cargaisonId));
        return mapper.toDto(f);
    }

    @Transactional(readOnly = true)
    public byte[] genererPdf(Long cargaisonId) {
        Facture f = factureRepository.findByCargaisonId(cargaisonId)
                .orElseThrow(() -> BusinessException.notFound("FACTURE_NOT_FOUND",
                        "Aucune facture pour la cargaison " + cargaisonId));
        return renderPdf(f);
    }

    public byte[] renderPdf(Facture f) {
        Cargaison c = f.getCargaison();
        Map<String, Object> model = new HashMap<>();
        model.put("facture", Map.of(
                "numero",       f.getNumero(),
                "dateFacture",  f.getDateFacture().toString(),
                "montantHt",    f.getMontantHt(),
                "montantTva",   f.getMontantTva(),
                "montantTtc",   f.getMontantTtc(),
                "devise",       f.getDevise()
        ));
        Map<String, Object> cargaisonMap = new HashMap<>();
        cargaisonMap.put("numeroTracage", c.getNumeroTracage());
        cargaisonMap.put("nombreColis", c.getNombreColis());
        if (c.getObservations() != null) cargaisonMap.put("observations", c.getObservations());
        model.put("cargaison", cargaisonMap);

        Map<String, Object> clientMap = new HashMap<>();
        clientMap.put("nom", c.getClient().getNom());
        clientMap.put("prenom", c.getClient().getPrenom());
        if (c.getClient().getTelephone() != null) clientMap.put("telephone", c.getClient().getTelephone());
        if (c.getClient().getEmail() != null)     clientMap.put("email", c.getClient().getEmail());
        if (c.getClient().getAdresseLivraison() != null) clientMap.put("adresseLivraison", c.getClient().getAdresseLivraison());
        model.put("client", clientMap);

        String html = templateService.render("facture.ftl", model);
        return pdfService.htmlToPdf(html);
    }

    private Facture creer(Long cargaisonId) {
        Cargaison cargaison = cargaisonRepository.findById(cargaisonId)
                .orElseThrow(() -> BusinessException.notFound("CARGAISON_NOT_FOUND",
                        "Cargaison introuvable : " + cargaisonId));

        BigDecimal ht  = cargaison.getMontantTotal();
        BigDecimal tva = BigDecimal.ZERO;        // À adapter selon législation
        BigDecimal ttc = ht.add(tva);

        Facture f = Facture.builder()
                .numero(genererNumero())
                .cargaison(cargaison)
                .dateFacture(LocalDate.now())
                .montantHt(ht)
                .montantTva(tva)
                .montantTtc(ttc)
                .devise(cargaison.getDevise())
                .envoyeeEmail(false)
                .envoyeeWhatsapp(false)
                .build();
        log.info("Facture {} créée pour cargaison {}", f.getNumero(), cargaison.getNumeroTracage());
        return factureRepository.save(f);
    }

    private String genererNumero() {
        int annee = LocalDate.now().getYear();
        long count = factureRepository.countByAnnee(annee) + 1;
        String numero;
        do {
            numero = String.format("%s-%d-%06d", prefix, annee, count);
            count++;
        } while (factureRepository.findByNumero(numero).isPresent());
        return numero;
    }

    @Transactional
    public void marquerEnvoyeeEmail(Long factureId, java.time.Instant date) {
        factureRepository.findById(factureId).ifPresent(f -> {
            f.setEnvoyeeEmail(true);
            f.setDateEnvoi(date);
            factureRepository.save(f);
        });
    }
}
