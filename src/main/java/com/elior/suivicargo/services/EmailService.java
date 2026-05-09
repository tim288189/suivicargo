package com.elior.suivicargo.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.notifications.email.from:no-reply@suivicargo.local}")
    private String defaultFrom;

    @Value("${app.notifications.email.enabled:true}")
    private boolean enabled;

    /**
     * Envoie un email HTML avec une pièce jointe (typiquement un PDF de facture).
     */
    public void envoyerAvecPieceJointe(String to,
                                       String subject,
                                       String htmlBody,
                                       String filename,
                                       byte[] attachment) throws MessagingException {
        if (!enabled) {
            log.warn("Notification email désactivée — destinataire {} ignoré", to);
            return;
        }
        if (to == null || to.isBlank()) {
            log.warn("Email destinataire vide — envoi ignoré");
            return;
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(defaultFrom);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        if (attachment != null && attachment.length > 0) {
            helper.addAttachment(filename, new ByteArrayResource(attachment));
        }
        mailSender.send(message);
        log.info("Email envoyé à {} avec sujet « {} »", to, subject);
    }
}
