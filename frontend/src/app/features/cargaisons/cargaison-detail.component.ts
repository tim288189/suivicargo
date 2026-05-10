import { DatePipe, DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, Input, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { SelectModule } from 'primeng/select';
import { InputTextModule } from 'primeng/inputtext';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';

import { CargaisonsService } from '@api-gen/cargaisons/cargaisons.service';
import { VoyagesService }    from '@api-gen/voyages/voyages.service';
import {
  CargaisonDetailDto,
  StatutCargaison,
  VoyageDto
} from '@api-gen/schemas';
import { AuthService } from '@core/auth/auth.service';
import { Role } from '@core/models/role.enum';

@Component({
  selector: 'app-cargaison-detail',
  standalone: true,
  imports: [
    DatePipe, DecimalPipe, RouterLink, ReactiveFormsModule, FormsModule,
    ButtonModule, TagModule, DialogModule, SelectModule, InputTextModule, ToastModule
  ],
  providers: [MessageService],
  template: `
    <p-toast></p-toast>

    @if (loading()) {
      <div class="text-gray-500">Chargement…</div>
    } @else if (notFound()) {
      <div class="bg-red-50 border border-red-200 text-red-800 rounded p-4">
        Cargaison introuvable.
        <a routerLink="/app/cargaisons" class="underline ml-2">Retour à la liste</a>
      </div>
    } @else if (data(); as c) {

      <!-- ===== Header ===== -->
      <div class="flex flex-wrap items-start justify-between gap-3 mb-6">
        <div>
          <a routerLink="/app/cargaisons" class="text-sm text-gray-500 hover:text-gray-700">
            <i class="pi pi-arrow-left text-xs"></i> Toutes les cargaisons
          </a>
          <h1 class="text-2xl font-bold text-gray-900 mt-2">
            {{ c.numeroTracage }}
          </h1>
          <div class="mt-2 flex items-center gap-2">
            <p-tag [value]="statutLabel(c.statut!)" [severity]="severityFor(c.statut!)"></p-tag>
            @if (isEnRetard()) {
              <p-tag value="En retard" severity="danger"></p-tag>
            }
            @if (c.factureEnvoyee) {
              <p-tag value="Facturée" severity="success"></p-tag>
            }
          </div>
        </div>

        <div class="flex gap-2">
          @if (canChangeStatus()) {
            <button pButton icon="pi pi-sync" label="Changer le statut"
                    (click)="openStatusDialog()"></button>
          }
          <button pButton icon="pi pi-file-pdf" label="Télécharger facture"
                  class="p-button-outlined"
                  [disabled]="!c.factureEnvoyee"
                  (click)="downloadFacture()"></button>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">

        <!-- ===== Colonne principale ===== -->
        <div class="lg:col-span-2 space-y-6">

          <!-- Infos cargaison -->
          <section class="bg-white rounded-lg shadow p-5">
            <h2 class="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-4">
              Cargaison
            </h2>
            <dl class="grid grid-cols-2 gap-x-6 gap-y-3 text-sm">
              <div>
                <dt class="text-gray-500">Date d'enlèvement</dt>
                <dd class="font-medium">{{ c.dateEnlevement | date: 'longDate' }}</dd>
              </div>
              <div>
                <dt class="text-gray-500">Livraison estimée</dt>
                <dd class="font-medium">
                  {{ c.dateLivraisonEstimee | date: 'longDate' }}
                  <span class="text-xs text-gray-500 ml-1">({{ joursRestants() }})</span>
                </dd>
              </div>
              @if (c.dateLivraisonReelle) {
                <div>
                  <dt class="text-gray-500">Livraison réelle</dt>
                  <dd class="font-medium text-green-700">
                    {{ c.dateLivraisonReelle | date: 'longDate' }}
                  </dd>
                </div>
              }
              <div>
                <dt class="text-gray-500">Nombre de colis</dt>
                <dd class="font-medium">{{ c.nombreColis }}</dd>
              </div>
              @if (c.poidsKg != null) {
                <div>
                  <dt class="text-gray-500">Poids</dt>
                  <dd class="font-medium">{{ c.poidsKg | number: '1.0-3' }} kg</dd>
                </div>
              }
              @if (c.volumeM3 != null) {
                <div>
                  <dt class="text-gray-500">Volume</dt>
                  <dd class="font-medium">{{ c.volumeM3 | number: '1.0-3' }} m³</dd>
                </div>
              }
              @if (c.conteneurNumero) {
                <div class="col-span-2">
                  <dt class="text-gray-500">Conteneur</dt>
                  <dd class="font-mono">{{ c.conteneurNumero }}</dd>
                </div>
              }
              @if (c.observations) {
                <div class="col-span-2">
                  <dt class="text-gray-500">Observations</dt>
                  <dd>{{ c.observations }}</dd>
                </div>
              }
            </dl>
          </section>

          <!-- Historique de statut -->
          <section class="bg-white rounded-lg shadow p-5">
            <h2 class="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-4">
              Historique
            </h2>
            <ol class="relative border-l-2 border-blue-200 ml-2 space-y-4">
              @for (e of c.historique; track e.id) {
                <li class="ml-5">
                  <span class="absolute -left-[7px] mt-1 w-3 h-3 rounded-full bg-blue-500"></span>
                  <div class="text-sm">
                    <span class="font-semibold">{{ statutLabel(e.nouveauStatut!) }}</span>
                    @if (e.ancienStatut) {
                      <span class="text-gray-400 mx-1">←</span>
                      <span class="text-gray-500">{{ statutLabel(e.ancienStatut) }}</span>
                    }
                    <span class="text-gray-500 ml-2">
                      {{ e.dateChangement | date: 'medium' }}
                    </span>
                    @if (e.auteur) {
                      <span class="text-gray-400 ml-1">— {{ e.auteur }}</span>
                    }
                  </div>
                  @if (e.commentaire) {
                    <p class="text-sm text-gray-600 mt-1">{{ e.commentaire }}</p>
                  }
                </li>
              } @empty {
                <li class="ml-5 text-gray-500 italic">Aucune entrée dans l'historique.</li>
              }
            </ol>
          </section>
        </div>

        <!-- ===== Sidebar ===== -->
        <div class="space-y-6">

          <!-- Client -->
          <section class="bg-white rounded-lg shadow p-5">
            <h2 class="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-3">
              Client
            </h2>
            <div class="font-semibold text-gray-900">
              {{ c.clientPrenom }} {{ c.clientNom }}
            </div>
            @if (c.clientTelephone) {
              <div class="text-sm text-gray-700 mt-1">
                <i class="pi pi-phone text-xs mr-1"></i> {{ c.clientTelephone }}
              </div>
            }
            @if (c.clientEmail) {
              <div class="text-sm text-gray-700 mt-1">
                <i class="pi pi-envelope text-xs mr-1"></i> {{ c.clientEmail }}
              </div>
            }
            @if (c.adresseEnlevement) {
              <div class="mt-3 text-xs text-gray-500 uppercase">Adresse d'enlèvement</div>
              <div class="text-sm">{{ c.adresseEnlevement }}</div>
            }
            @if (c.adresseLivraison) {
              <div class="mt-2 text-xs text-gray-500 uppercase">Adresse de livraison</div>
              <div class="text-sm">{{ c.adresseLivraison }}</div>
            }
          </section>

          <!-- Voyage -->
          <section class="bg-white rounded-lg shadow p-5">
            <div class="flex items-center justify-between mb-3">
              <h2 class="text-sm font-semibold text-gray-500 uppercase tracking-wider">
                Voyage
              </h2>
              @if (canChangeStatus()) {
                <button pButton type="button" icon="pi pi-pencil"
                        [label]="c.voyageId ? 'Modifier' : 'Assigner'"
                        class="p-button-text p-button-sm"
                        (click)="openVoyageDialog()"></button>
              }
            </div>
            @if (c.voyageId) {
              <div class="space-y-1 text-sm">
                <div class="font-semibold text-gray-900">
                  {{ c.voyageNavireNom }}
                </div>
                <div class="text-gray-700">
                  <i class="pi pi-arrow-right-arrow-left text-xs mr-1"></i>
                  {{ c.voyagePortDepart }} → {{ c.voyagePortArrivee }}
                </div>
                @if (c.voyageDateDepart) {
                  <div class="text-xs text-gray-500">
                    Départ : {{ c.voyageDateDepart | date: 'shortDate' }}
                    @if (c.voyageEtaArrivee) {
                      · ETA : {{ c.voyageEtaArrivee | date: 'shortDate' }}
                    }
                  </div>
                }
                @if (c.voyageStatut) {
                  <div class="mt-2">
                    <p-tag [value]="c.voyageStatut!"
                           [severity]="voyageSeverityFor(c.voyageStatut!)"></p-tag>
                  </div>
                }
              </div>
            } @else {
              <div class="text-sm text-gray-500 italic">
                Cargaison non encore affectée à un voyage.
              </div>
            }
          </section>

          <!-- Paiement -->
          <section class="bg-white rounded-lg shadow p-5">
            <h2 class="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-3">
              Paiement
            </h2>
            <div class="space-y-2 text-sm">
              <div class="flex justify-between">
                <span class="text-gray-500">Total</span>
                <span class="font-medium">
                  {{ c.montantTotal | number: '1.0-0' }} {{ c.devise }}
                </span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-500">Réglé</span>
                <span class="font-medium text-green-700">
                  {{ c.montantRegle | number: '1.0-0' }} {{ c.devise }}
                </span>
              </div>
              <div class="flex justify-between pt-2 border-t border-gray-100">
                <span class="font-semibold">Reste à payer</span>
                <span class="font-bold"
                      [class.text-red-600]="(c.montantRestant ?? 0) > 0"
                      [class.text-gray-700]="(c.montantRestant ?? 0) === 0">
                  {{ c.montantRestant | number: '1.0-0' }} {{ c.devise }}
                </span>
              </div>
            </div>
            <div class="mt-3 h-2 bg-gray-100 rounded-full overflow-hidden">
              <div class="h-full bg-blue-500 transition-all"
                   [style.width.%]="progressPaiement()"></div>
            </div>
            <div class="text-xs text-gray-500 mt-1">{{ progressPaiement() }}% réglé</div>
          </section>

        </div>
      </div>

      <!-- ===== Dialog assignation voyage ===== -->
      <p-dialog [(visible)]="voyageDialogVisible" header="Assigner à un voyage"
                [modal]="true" [style]="{ width: '36rem' }" [closable]="!savingVoyage()">
        <div class="space-y-3">
          <p class="text-sm text-gray-600">
            Sélectionne un voyage parmi ceux programmés ou en cours.
            Le statut de la cargaison s'alignera sur celui du voyage à chaque mise à jour.
          </p>
          <p-select [options]="voyageOptions()" [(ngModel)]="voyageSelected"
                    optionLabel="label" optionValue="value"
                    placeholder="Choisir un voyage"
                    [style]="{ width: '100%' }"
                    [showClear]="true"></p-select>
          <div class="flex justify-end gap-2 pt-2">
            <button pButton type="button" label="Annuler" class="p-button-text"
                    (click)="voyageDialogVisible.set(false)"
                    [disabled]="savingVoyage()"></button>
            @if (data()?.voyageId) {
              <button pButton type="button" label="Détacher" class="p-button-outlined p-button-danger"
                      (click)="submitVoyageChange(null)"
                      [disabled]="savingVoyage()"
                      [loading]="savingVoyage()"></button>
            }
            <button pButton type="button" label="Assigner"
                    (click)="submitVoyageChange(voyageSelected)"
                    [disabled]="!voyageSelected || savingVoyage()"
                    [loading]="savingVoyage()"></button>
          </div>
        </div>
      </p-dialog>

      <!-- ===== Dialog changement de statut ===== -->
      <p-dialog [(visible)]="statusDialogVisible" header="Changer le statut"
                [modal]="true" [style]="{ width: '32rem' }" [closable]="!savingStatus()">
        <form [formGroup]="statusForm" (ngSubmit)="submitStatusChange()" class="flex flex-col gap-4">
          <div>
            <label class="block text-sm font-medium mb-1">Nouveau statut *</label>
            <p-select formControlName="statut" [options]="statutOptions"
                      placeholder="Sélectionner" [style]="{ width: '100%' }"></p-select>
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Commentaire</label>
            <textarea pInputText formControlName="commentaire" rows="3"
                      class="w-full px-3 py-2 border border-gray-300 rounded"
                      placeholder="Optionnel — sera visible dans l'historique"></textarea>
          </div>
          <div class="flex justify-end gap-2 pt-2">
            <button pButton type="button" label="Annuler" class="p-button-text"
                    (click)="statusDialogVisible.set(false)" [disabled]="savingStatus()"></button>
            <button pButton type="submit" label="Enregistrer"
                    [disabled]="statusForm.invalid || savingStatus()"
                    [loading]="savingStatus()"></button>
          </div>
        </form>
      </p-dialog>
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CargaisonDetailComponent {

  private readonly api        = inject(CargaisonsService);
  private readonly voyagesApi = inject(VoyagesService);
  private readonly auth       = inject(AuthService);
  private readonly router     = inject(Router);
  private readonly fb         = inject(FormBuilder);
  private readonly toast      = inject(MessageService);

  protected readonly data       = signal<CargaisonDetailDto | null>(null);
  protected readonly loading    = signal(true);
  protected readonly notFound   = signal(false);

  protected readonly statusDialogVisible = signal(false);
  protected readonly savingStatus        = signal(false);

  // ---- Assignation voyage ----
  protected readonly voyageDialogVisible = signal(false);
  protected readonly savingVoyage        = signal(false);
  protected readonly voyageOptions       = signal<Array<{ label: string; value: number }>>([]);
  protected voyageSelected: number | null = null;

  /** Lié à la route /app/cargaisons/:id par withComponentInputBinding(). */
  @Input() set id(value: string) {
    this.fetch(Number(value));
  }

  protected readonly statutOptions = [
    { label: 'Enlevé chez le client',  value: 'ENLEVE' },
    { label: 'En entrepôt',            value: 'EN_ENTREPOT' },
    { label: 'Chargé sur navire',      value: 'CHARGE_NAVIRE' },
    { label: 'En mer',                 value: 'EN_MER' },
    { label: 'Au port d\'arrivée',     value: 'AU_PORT_ARRIVEE' },
    { label: 'En douane',              value: 'EN_DOUANE' },
    { label: 'Livré',                  value: 'LIVRE' },
    { label: 'Annulé',                 value: 'ANNULE' }
  ];

  protected readonly statusForm = this.fb.nonNullable.group({
    statut:      ['' as StatutCargaison | '', Validators.required],
    commentaire: ['']
  });

  protected canChangeStatus(): boolean {
    return this.auth.hasRole([Role.SUPERVISOR, Role.ADMIN]);
  }

  protected statutLabel(s: StatutCargaison | string): string {
    return this.statutOptions.find(o => o.value === s)?.label ?? String(s);
  }

  protected severityFor(statut: string): 'success' | 'info' | 'warn' | 'danger' | 'contrast' {
    switch (statut) {
      case 'LIVRE':              return 'success';
      case 'EN_DOUANE':
      case 'EN_MER':             return 'warn';
      case 'ANNULE':             return 'danger';
      case 'AU_PORT_ARRIVEE':    return 'info';
      default:                   return 'contrast';
    }
  }

  protected joursRestants(): string {
    const c = this.data();
    if (!c?.dateLivraisonEstimee) return '';
    const eta = new Date(c.dateLivraisonEstimee as unknown as string);
    const now = new Date();
    const diff = Math.ceil((eta.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
    if (diff > 0) return `dans ${diff} j`;
    if (diff === 0) return "aujourd'hui";
    return `il y a ${-diff} j`;
  }

  protected isEnRetard(): boolean {
    const c = this.data();
    if (!c?.dateLivraisonEstimee || c.statut === 'LIVRE' || c.statut === 'ANNULE') return false;
    const eta = new Date(c.dateLivraisonEstimee as unknown as string);
    return eta.getTime() < Date.now();
  }

  protected progressPaiement(): number {
    const c = this.data();
    if (!c || !c.montantTotal) return 0;
    const total = Number(c.montantTotal);
    const regle = Number(c.montantRegle ?? 0);
    if (total <= 0) return 0;
    return Math.min(100, Math.round((regle / total) * 100));
  }

  protected voyageSeverityFor(s: string): 'success' | 'info' | 'warn' | 'danger' | 'contrast' {
    switch (s) {
      case 'ARRIVE':    return 'success';
      case 'EN_MER':    return 'warn';
      case 'ANNULE':    return 'danger';
      case 'PROGRAMME': return 'info';
      default:          return 'contrast';
    }
  }

  protected openVoyageDialog(): void {
    this.voyageSelected = this.data()?.voyageId ?? null;
    this.voyageDialogVisible.set(true);
    // Charge les voyages PROGRAMME ou EN_MER
    this.voyagesApi.listVoyages({ size: 200 } as never).subscribe({
      next: (page: any) => {
        const opts = (page.content ?? [])
          .filter((v: VoyageDto) => v.statut === 'PROGRAMME' || v.statut === 'EN_MER')
          .map((v: VoyageDto) => ({
            label: `${v.navireNom} — ${v.portDepart} → ${v.portArrivee}`
                 + ` (départ ${v.dateDepart})`,
            value: v.id!
          }));
        this.voyageOptions.set(opts);
      }
    });
  }

  protected submitVoyageChange(voyageId: number | null): void {
    const c = this.data();
    if (!c) return;
    this.savingVoyage.set(true);

    this.api.assignerVoyageCargaison(c.id!, { voyageId: voyageId ?? undefined } as never).subscribe({
      next: () => {
        this.toast.add({
          severity: 'success',
          summary: voyageId ? 'Voyage assigné' : 'Cargaison détachée'
        });
        this.voyageDialogVisible.set(false);
        this.savingVoyage.set(false);
        this.fetch(c.id!);
      },
      error: () => {
        this.toast.add({ severity: 'error', summary: 'Erreur', detail: 'Action impossible' });
        this.savingVoyage.set(false);
      }
    });
  }

  protected openStatusDialog(): void {
    this.statusForm.reset({ statut: '', commentaire: '' });
    this.statusDialogVisible.set(true);
  }

  protected submitStatusChange(): void {
    const v = this.statusForm.getRawValue();
    if (!v.statut) return;
    const c = this.data();
    if (!c) return;
    this.savingStatus.set(true);

    this.api.changerStatutCargaison(c.id!, {
      statut: v.statut as StatutCargaison,
      commentaire: v.commentaire || undefined
    } as never).subscribe({
      next: () => {
        this.toast.add({ severity: 'success', summary: 'Statut mis à jour', detail: this.statutLabel(v.statut) });
        this.statusDialogVisible.set(false);
        this.savingStatus.set(false);
        this.fetch(c.id!);
      },
      error: () => {
        this.toast.add({ severity: 'error', summary: 'Erreur', detail: 'Mise à jour impossible' });
        this.savingStatus.set(false);
      }
    });
  }

  protected downloadFacture(): void {
    const c = this.data();
    if (!c?.factureEnvoyee || !c.id) return;
    // L'API renvoie un PDF binaire ; on l'ouvre dans un nouvel onglet via le mutator HttpClient.
    // Simple fallback : un click direct sur l'URL avec le JWT en query (pas idéal en prod).
    // En pratique, on appellera plutôt FacturesService.downloadFacturePdf et on créera un Blob.
    window.open(`/api/v1/factures/by-cargaison/${c.id}/pdf`, '_blank');
  }

  private fetch(id: number): void {
    this.loading.set(true);
    this.notFound.set(false);
    this.api.getCargaisonById(id).subscribe({
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
