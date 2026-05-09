package com.elior.suivicargo.services;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {

    private final Configuration freemarkerConfiguration;

    /**
     * Rend un template FreeMarker en chaîne HTML/texte.
     *
     * @param templateName nom du template (ex: "facture.ftl")
     * @param model        modèle de données à injecter
     */
    public String render(String templateName, Map<String, Object> model) {
        try {
            Template template = freemarkerConfiguration.getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.process(model, writer);
            return writer.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Échec de rendu du template " + templateName, ex);
        }
    }
}
