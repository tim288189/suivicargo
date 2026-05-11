import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';

/**
 * Service wrapper pour télécharger les factures PDF en Blob.
 *
 * Utilise HttpClient directement avec responseType 'blob' car le mutator Orval
 * force responseType 'json' et ne peut pas retourner un Blob.
 * L'authInterceptor ajoute automatiquement le Bearer JWT à chaque requête.
 */
@Injectable({ providedIn: 'root' })
export class FacturesDownloadService {
  private readonly http = inject(HttpClient);

  /**
   * Récupère le PDF de la facture liée à une cargaison.
   * GET /v1/factures/by-cargaison/{cargaisonId}/pdf
   */
  downloadFacturePdf(cargaisonId: number): Observable<Blob> {
    return this.http.get(
      `${environment.apiUrl}/v1/factures/by-cargaison/${cargaisonId}/pdf`,
      { responseType: 'blob' }
    );
  }
}
