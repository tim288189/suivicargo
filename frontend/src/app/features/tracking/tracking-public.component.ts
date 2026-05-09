import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, Input, signal } from '@angular/core';
import { TrackingPublicService } from '@api-gen/tracking-public/tracking-public.service';
import { TrackingPublicResponse } from '@api-gen/schemas';

@Component({
  selector: 'app-tracking-public',
  standalone: true,
  imports: [DatePipe],
  template: `
    <div class="max-w-3xl mx-auto px-6 py-10">
      <h1 class="text-2xl font-bold mb-6">Suivi de cargaison</h1>

      @if (loading()) {
        <div class="text-gray-500">Chargement…</div>
      } @else if (notFound()) {
        <div class="bg-yellow-50 border border-yellow-200 rounded p-4 text-yellow-800">
          Aucune cargaison ne correspond à ce numéro de traçage.
        </div>
      } @else if (data(); as t) {
        <div class="bg-white rounded-lg shadow p-6 mb-6">
          <div class="text-xs uppercase text-gray-500">Numéro de traçage</div>
          <div class="font-mono text-lg font-bold">{{ t.numeroTracage }}</div>
          <div class="mt-4 grid grid-cols-2 gap-4 text-sm">
            <div>
              <div class="text-gray-500">Statut actuel</div>
              <div class="font-semibold">{{ t.statutActuel }}</div>
            </div>
            <div>
              <div class="text-gray-500">Nombre de colis</div>
              <div class="font-semibold">{{ t.nombreColis }}</div>
            </div>
            @if (t.portDepart) {
              <div>
                <div class="text-gray-500">Port de départ</div>
                <div class="font-semibold">{{ t.portDepart }}</div>
              </div>
            }
            @if (t.portArrivee) {
              <div>
                <div class="text-gray-500">Port d'arrivée</div>
                <div class="font-semibold">{{ t.portArrivee }}</div>
              </div>
            }
          </div>
        </div>

        <h2 class="text-lg font-semibold mb-3">Historique</h2>
        <ol class="border-l-2 border-blue-200 ml-2 space-y-4 relative pl-6">
          @for (e of t.historique; track e.dateChangement) {
            <li>
              <div class="absolute -ml-3 mt-1 w-3 h-3 rounded-full bg-blue-500"></div>
              <div class="text-sm">
                <span class="font-semibold">{{ e.statut }}</span>
                <span class="text-gray-500 ml-2">{{ e.dateChangement | date: 'medium' }}</span>
              </div>
              @if (e.commentaire) {
                <div class="text-sm text-gray-600">{{ e.commentaire }}</div>
              }
            </li>
          }
        </ol>
      }
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TrackingPublicComponent {

  private readonly api = inject(TrackingPublicService);

  protected readonly data = signal<TrackingPublicResponse | null>(null);
  protected readonly loading = signal(true);
  protected readonly notFound = signal(false);

  /** Liaison automatique au paramètre :numeroTracage de la route. */
  @Input() set numeroTracage(value: string) {
    this.fetch(value);
  }

  private fetch(numero: string): void {
    this.loading.set(true);
    this.notFound.set(false);
    this.api.trackPublic(numero).subscribe({
      next: (res) => {
        this.data.set(res);
        this.loading.set(false);
      },
      error: () => {
        this.notFound.set(true);
        this.loading.set(false);
      }
    });
  }
}
