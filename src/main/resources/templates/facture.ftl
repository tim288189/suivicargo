<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" lang="fr">
<head>
    <meta charset="UTF-8"/>
    <title>Facture ${facture.numero}</title>
    <style type="text/css">
        @page {
            size: A4;
            margin: 20mm;
        }
        body {
            font-family: 'Helvetica', 'Arial', sans-serif;
            font-size: 10pt;
            color: #1f2937;
            line-height: 1.5;
        }
        .header {
            border-bottom: 2px solid #0f4c81;
            padding-bottom: 15px;
            margin-bottom: 25px;
        }
        .header table {
            width: 100%;
            border-collapse: collapse;
        }
        .brand {
            font-size: 22pt;
            font-weight: bold;
            color: #0f4c81;
            margin: 0;
        }
        .brand-sub {
            font-size: 9pt;
            color: #6b7280;
            margin: 0;
        }
        .doc-title {
            text-align: right;
        }
        .doc-title h2 {
            font-size: 18pt;
            color: #0f4c81;
            margin: 0;
        }
        .doc-meta {
            font-size: 9pt;
            color: #4b5563;
        }
        .info-block {
            display: block;
            margin-bottom: 25px;
        }
        .info-block table {
            width: 100%;
            border-collapse: collapse;
        }
        .info-block td {
            vertical-align: top;
            padding: 0;
            width: 50%;
        }
        .label {
            font-size: 8.5pt;
            text-transform: uppercase;
            color: #6b7280;
            letter-spacing: 0.5px;
            margin-bottom: 4px;
            font-weight: bold;
        }
        .info-card {
            background: #f9fafb;
            border-left: 3px solid #0f4c81;
            padding: 10px 14px;
            margin-right: 10px;
        }
        .items-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }
        .items-table th {
            background: #0f4c81;
            color: white;
            padding: 10px;
            text-align: left;
            font-size: 9pt;
            font-weight: bold;
        }
        .items-table td {
            padding: 10px;
            border-bottom: 1px solid #e5e7eb;
        }
        .items-table .right { text-align: right; }
        .items-table .center { text-align: center; }

        .totaux {
            width: 50%;
            margin-left: 50%;
            margin-top: 20px;
            border-collapse: collapse;
        }
        .totaux td {
            padding: 6px 10px;
            font-size: 10pt;
        }
        .totaux .label-cell {
            text-align: right;
            color: #4b5563;
        }
        .totaux .value-cell {
            text-align: right;
            font-weight: bold;
        }
        .totaux .ttc-row {
            background: #0f4c81;
            color: white;
            font-size: 12pt;
        }
        .footer {
            margin-top: 40px;
            padding-top: 15px;
            border-top: 1px solid #e5e7eb;
            font-size: 8.5pt;
            color: #6b7280;
            text-align: center;
        }
        .tracking-box {
            background: #fef3c7;
            border: 1px solid #f59e0b;
            padding: 10px;
            margin-top: 15px;
            text-align: center;
            font-size: 9pt;
        }
        .tracking-num {
            font-family: monospace;
            font-size: 13pt;
            font-weight: bold;
            color: #b45309;
        }
    </style>
</head>
<body>

<div class="header">
    <table>
        <tr>
            <td>
                <p class="brand">SUIVICARGO</p>
                <p class="brand-sub">Fret maritime &amp; logistique</p>
            </td>
            <td class="doc-title">
                <h2>FACTURE</h2>
                <div class="doc-meta">
                    N° <strong>${facture.numero}</strong><br/>
                    Date : ${facture.dateFacture}
                </div>
            </td>
        </tr>
    </table>
</div>

<div class="info-block">
    <table>
        <tr>
            <td>
                <div class="label">Émetteur</div>
                <div class="info-card">
                    <strong>Suivicargo SARL</strong><br/>
                    Adresse de l'entreprise<br/>
                    Téléphone : +XXX XXX XXX XXX<br/>
                    Email : contact@suivicargo.local
                </div>
            </td>
            <td>
                <div class="label">Client</div>
                <div class="info-card">
                    <strong>${client.prenom} ${client.nom}</strong><br/>
                    <#if client.telephone??>${client.telephone}<br/></#if>
                    <#if client.email??>${client.email}<br/></#if>
                    <#if client.adresseLivraison??>${client.adresseLivraison}</#if>
                </div>
            </td>
        </tr>
    </table>
</div>

<table class="items-table">
    <thead>
    <tr>
        <th>Désignation</th>
        <th class="center">Quantité</th>
        <th class="right">Montant unitaire</th>
        <th class="right">Total</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td>
            Cargaison fret maritime<br/>
            <small>Numéro de traçage : <strong>${cargaison.numeroTracage}</strong></small><br/>
            <#if cargaison.observations??><small>${cargaison.observations}</small></#if>
        </td>
        <td class="center">${cargaison.nombreColis} colis</td>
        <td class="right">—</td>
        <td class="right">${facture.montantHt} ${facture.devise}</td>
    </tr>
    </tbody>
</table>

<table class="totaux">
    <tr>
        <td class="label-cell">Total HT</td>
        <td class="value-cell">${facture.montantHt} ${facture.devise}</td>
    </tr>
    <tr>
        <td class="label-cell">TVA</td>
        <td class="value-cell">${facture.montantTva} ${facture.devise}</td>
    </tr>
    <tr class="ttc-row">
        <td class="label-cell">Total TTC</td>
        <td class="value-cell">${facture.montantTtc} ${facture.devise}</td>
    </tr>
</table>

<div class="tracking-box">
    Suivez votre cargaison en ligne avec le numéro :
    <span class="tracking-num">${cargaison.numeroTracage}</span>
</div>

<div class="footer">
    Merci de votre confiance.<br/>
    Cette facture est générée automatiquement par Suivicargo. En cas de question, contactez-nous.
</div>

</body>
</html>
