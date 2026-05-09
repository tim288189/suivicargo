package com.elior.suivicargo.services;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Slf4j
@Service
public class PdfService {

    /**
     * Convertit un HTML "XHTML strict" en bytes PDF via OpenHTMLtoPDF.
     *
     * <p>Note : le HTML doit être bien formé (XHTML strict). Tous les tags doivent
     * être fermés (<code>&lt;br/&gt;</code>, <code>&lt;img/&gt;</code>) et un DOCTYPE
     * doit être présent.
     */
    public byte[] htmlToPdf(String html) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Échec de génération du PDF", ex);
        }
    }
}
