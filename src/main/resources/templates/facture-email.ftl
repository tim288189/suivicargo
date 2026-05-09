<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" lang="fr">
<head>
    <meta charset="UTF-8"/>
    <title>Facture ${facture.numero}</title>
</head>
<body style="margin:0;padding:0;font-family:Helvetica,Arial,sans-serif;color:#1f2937;">
<div style="max-width:600px;margin:auto;padding:20px;">
    <div style="border-bottom:2px solid #0f4c81;padding-bottom:10px;">
        <h1 style="color:#0f4c81;margin:0;">Suivicargo</h1>
    </div>
    <p>Bonjour ${client.prenom} ${client.nom},</p>
    <p>
        Nous vous remercions pour votre confiance.<br/>
        Le règlement complet de votre cargaison
        <strong>${cargaison.numeroTracage}</strong> a bien été reçu.
    </p>
    <p>
        Vous trouverez en pièce jointe la facture
        <strong>${facture.numero}</strong> du ${facture.dateFacture}
        pour un montant TTC de <strong>${facture.montantTtc} ${facture.devise}</strong>.
    </p>
    <div style="background:#f9fafb;border-left:3px solid #0f4c81;padding:12px;margin:20px 0;">
        <strong>Suivi en ligne :</strong><br/>
        Vous pouvez suivre votre cargaison à tout moment avec le numéro de traçage
        <code>${cargaison.numeroTracage}</code>.
    </div>
    <p>Bien cordialement,<br/>L'équipe Suivicargo</p>
    <hr style="border:none;border-top:1px solid #e5e7eb;margin:20px 0;"/>
    <p style="font-size:11px;color:#6b7280;">
        Cet email a été généré automatiquement. Merci de ne pas y répondre directement.
    </p>
</div>
</body>
</html>
