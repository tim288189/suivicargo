package com.elior.suivicargo.config;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.context.annotation.Bean;

import java.nio.charset.StandardCharsets;

@org.springframework.context.annotation.Configuration
public class FreemarkerConfig {

    /**
     * Configuration FreeMarker dédiée à la génération de PDF + emails.
     * Templates dans <code>classpath:/templates/</code>.
     */
    @Bean
    public Configuration freemarkerConfiguration() {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_34);
        cfg.setClassLoaderForTemplateLoading(getClass().getClassLoader(), "templates");
        cfg.setDefaultEncoding(StandardCharsets.UTF_8.name());
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
        return cfg;
    }
}
