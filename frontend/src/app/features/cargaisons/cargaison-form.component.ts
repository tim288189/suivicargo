import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { DialogModule } from 'primeng/dialog';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';

import { CargaisonsService } from '@api-gen/cargaisons/cargaisons.service';
import { ClientsService }    from '@api-gen/clients/clients.service';
import {
  ClientDto,
  CreateCargaisonRequest,
  CreateClientRequest
} from '@api-gen/schemas';

type ClientSuggestion = ClientDto & { nomComplet: string };

@Component({
  selector: 'app-cargaison-form',
  standalone: true,
  imports: [
    ReactiveFormsModule, ButtonModule, InputTextModule, InputNumberModule,
    SelectModule, AutoCompleteModule, DialogModule, ToastModule
  ],
  providers: [MessageService],
  template: `
    <p-toast></p-toast>
    <h1 class="text-2xl font-bold text-gray-900 mb-6">Nouvelle cargaison à l'enlèvement</h1>

    <form [formGroup]="form" (ngSubmit)="submit()"
          class="bg-white rounded-lg shadow p-6 max-w-2xl space-y-5">

      <!-- ===== Sélection / création client ===== -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Client *</label>
        <div class="flex gap-2">
          <p-autocomplete formControlName="client"
                          [suggestions]="clientSuggestions()"
                          (completeMethod)="searchClients($event)"
                          optionLabel="nomComplet"
                          [forceSelection]="true"
                          placeholder="Rechercher par nom, prénom ou téléphone"
                          [style]="{ width: '100%' }"
                          [inputStyle]="{ width: '100%' }"
                          appendTo="body"
                          emptyMessage="Aucun client trouvé">
            <ng-template let-c #item>
              <div class="flex flex-col py-1">
                <span class="font-medium">{{ c.prenom }} {{ c.nom }}</span>
                @if (c.telephone) {
                  <span class="text-xs text-gray-500">{{ c.telephone }}</span>
                }
              </div>
            </ng-template>
            <ng-template let-c #selectedItem>
              <span>{{ c.prenom }} {{ c.nom }}<span class="text-gray-500"> ({{ c.telephone }})</span></span>
            </ng-template>
          </p-autocomplete>
          <button pButton type="button" icon="pi pi-plus" label="Nouveau client"
                  class="p-button-outlined whitespace-nowrap"
                  (click)="openNewClientDialog()"></button>
        </div>
        @if (lastSearchEmpty() && lastSearchQuery()) {
          <div class="mt-2 text-sm text-amber-700 bg-amber-50 border border-amber-200 rounded px-3 py-2">
            Aucun client trouvé pour « {{ lastSearchQuery() }} ».
            <button type="button" class="underline font-semibold ml-1"
                    (click)="openNewClientDialog(lastSearchQuery())">
              Créer ce client maintenant
            </button>
          </div>
        }
      </div>

      <!-- ===== Détails cargaison ===== -->
      <div class="grid grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Nombre de colis *</label>
          <p-inputNumber formControlName="nombreColis" [min]="1" [showButtons]="true"></p-inputNumber>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Devise</label>
          <p-select formControlName="devise" [options]="deviseOptions" [style]="{ width: '100%' }"></p-select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Poids (kg)</label>
          <p-inputNumber formControlName="poidsKg" mode="decimal" [minFractionDigits]="0" [maxFractionDigits]="3"></p-inputNumber>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Volume (m³)</label>
          <p-inputNumber formControlName="volumeM3" mode="decimal" [minFractionDigits]="0" [maxFractionDigits]="3"></p-inputNumber>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Montant total *</label>
          <p-inputNumber formControlName="montantTotal" [min]="0"></p-inputNumber>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Montant reçu à l'enlèvement *</label>
          <p-inputNumber formControlName="montantRegle" [min]="0"></p-inputNumber>
        </div>
      </div>

      <div class="bg-blue-50 border-l-4 border-blue-400 p-3 text-sm">
        <strong>Reste à payer :</strong>
        {{ montantRestant() }} {{ form.value.devise }}
      </div>

      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Observations</label>
        <textarea formControlName="observations" rows="3"
                  class="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-[#0f4c81]"></textarea>
      </div>

      <div class="flex justify-end gap-2">
        <button pButton type="button" label="Annuler" class="p-button-text" (click)="cancel()"></button>
        <button pButton type="submit" label="Enregistrer l'enlèvement"
                [disabled]="form.invalid || saving()" [loading]="saving()"></button>
      </div>
    </form>

    <!-- ===== Dialogue de création rapide d'un client ===== -->
    <p-dialog [(visible)]="newClientDialogVisible" header="Nouveau client"
              [modal]="true" [style]="{ width: '36rem' }" [closable]="!savingClient()">
      <form [formGroup]="newClientForm" (ngSubmit)="saveNewClient()" class="flex flex-col gap-3">
        <div class="grid grid-cols-2 gap-3">
          <div>
            <label class="block text-sm font-medium mb-1">Prénom *</label>
            <input pInputText formControlName="prenom" class="w-full" />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Nom *</label>
            <input pInputText formControlName="nom" class="w-full" />
          </div>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Téléphone *</label>
          <input pInputText formControlName="telephone" class="w-full" placeholder="+221701234567" />
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Email</label>
          <input pInputText type="email" formControlName="email" class="w-full" />
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Adresse d'enlèvement *</label>
          <textarea pInputText formControlName="adresseEnlevement" rows="2"
                    class="w-full px-3 py-2 border border-gray-300 rounded"></textarea>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Adresse de livraison *</label>
          <textarea pInputText formControlName="adresseLivraison" rows="2"
                    class="w-full px-3 py-2 border border-gray-300 rounded"></textarea>
        </div>

        <div class="flex justify-end gap-2 pt-2">
          <button pButton type="button" label="Annuler" class="p-button-text"
                  (click)="newClientDialogVisible.set(false)"
                  [disabled]="savingClient()"></button>
          <button pButton type="submit" label="Créer le client"
                  [disabled]="newClientForm.invalid || savingClient()"
                  [loading]="savingClient()"></button>
        </div>
      </form>
    </p-dialog>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CargaisonFormComponent {

  private readonly cargaisons = inject(CargaisonsService);
  private readonly clients    = inject(ClientsService);
  private readonly router     = inject(Router);
  private readonly fb         = inject(FormBuilder);
  private readonly toast      = inject(MessageService);

  // ---- état recherche / suggestions ----
  protected readonly saving = signal(false);
  protected readonly clientSuggestions = signal<ClientSuggestion[]>([]);
  protected readonly lastSearchEmpty   = signal(false);
  protected readonly lastSearchQuery   = signal<string>('');

  // ---- état dialog création client ----
  protected readonly newClientDialogVisible = signal(false);
  protected readonly savingClient = signal(false);

  protected readonly deviseOptions = [
    { label: 'XOF (Francs CFA)', value: 'XOF' },
    { label: 'EUR (Euro)',       value: 'EUR' },
    { label: 'USD (Dollar US)',  value: 'USD' }
  ];

  protected readonly form = this.fb.nonNullable.group({
    client:        [null as ClientDto | null, Validators.required],
    nombreColis:   [1, [Validators.required, Validators.min(1)]],
    poidsKg:       [null as number | null],
    volumeM3:      [null as number | null],
    montantTotal:  [0, [Validators.required, Validators.min(0)]],
    montantRegle:  [0, [Validators.required, Validators.min(0)]],
    devise:        ['XOF', Validators.required],
    observations:  ['']
  });

  protected readonly newClientForm = this.fb.nonNullable.group({
    prenom:            ['', [Validators.required, Validators.maxLength(100)]],
    nom:               ['', [Validators.required, Validators.maxLength(100)]],
    telephone:         ['', [Validators.required, Validators.pattern(/^\+?[0-9]{8,15}$/)]],
    email:             ['', [Validators.email]],
    adresseEnlevement: ['', [Validators.required, Validators.maxLength(500)]],
    adresseLivraison:  ['', [Validators.required, Validators.maxLength(500)]]
  });

  protected readonly montantRestant = computed(() => {
    const v = this.formValue();
    return Math.max(0, (v.montantTotal ?? 0) - (v.montantRegle ?? 0));
  });

  private readonly formValue = signal(this.form.getRawValue());

  constructor() {
    this.form.valueChanges.subscribe(() => this.formValue.set(this.form.getRawValue()));
  }

  /** Recherche un client à chaque saisie dans l'autocomplete. */
  protected searchClients(event: { query: string }): void {
    const q = (event.query ?? '').trim();
    this.lastSearchQuery.set(q);
    this.clients.searchClients({ q, size: 10 } as never).subscribe({
      next: (page: any) => {
        const enriched: ClientSuggestion[] = (page.content ?? []).map((c: ClientDto) => ({
          ...c,
          nomComplet: this.formatNomComplet(c)
        }));
        this.clientSuggestions.set(enriched);
        this.lastSearchEmpty.set(enriched.length === 0 && q.length > 0);
      },
      error: () => {
        this.clientSuggestions.set([]);
        this.lastSearchEmpty.set(false);
      }
    });
  }

  /** Ouvre le dialogue, en pré-remplissant le nom à partir de la recherche en cours. */
  protected openNewClientDialog(prefill?: string): void {
    this.newClientForm.reset({
      prenom: '', nom: '', telephone: '', email: '',
      adresseEnlevement: '', adresseLivraison: ''
    });
    if (prefill) {
      const parts = prefill.split(/\s+/).filter(Boolean);
      if (parts.length === 1) {
        this.newClientForm.patchValue({ nom: parts[0] });
      } else if (parts.length >= 2) {
        this.newClientForm.patchValue({ prenom: parts[0], nom: parts.slice(1).join(' ') });
      }
    }
    this.newClientDialogVisible.set(true);
  }

  /** Crée le client puis le sélectionne automatiquement dans l'autocomplete. */
  protected saveNewClient(): void {
    if (this.newClientForm.invalid) return;
    this.savingClient.set(true);
    const req = this.newClientForm.getRawValue() as CreateClientRequest;

    this.clients.createClient(req).subscribe({
      next: (created: any) => {
        const suggestion: ClientSuggestion = {
          ...created,
          nomComplet: this.formatNomComplet(created)
        };
        // Auto-sélection dans le formulaire principal
        this.form.controls.client.setValue(suggestion);
        this.clientSuggestions.set([suggestion]);
        this.lastSearchEmpty.set(false);

        this.toast.add({
          severity: 'success',
          summary: 'Client créé',
          detail: `${suggestion.prenom} ${suggestion.nom}`
        });
        this.newClientDialogVisible.set(false);
        this.savingClient.set(false);
      },
      error: (err) => {
        const detail = err?.error?.code === 'DATA_INTEGRITY'
          ? 'Un client avec ces informations existe déjà'
          : 'Création du client impossible';
        this.toast.add({ severity: 'error', summary: 'Erreur', detail });
        this.savingClient.set(false);
      }
    });
  }

  protected submit(): void {
    if (this.form.invalid) return;
    this.saving.set(true);
    const v = this.form.getRawValue();

    if (v.montantRegle > v.montantTotal) {
      this.toast.add({
        severity: 'warn',
        summary: 'Montant invalide',
        detail: 'Le montant réglé ne peut pas dépasser le total'
      });
      this.saving.set(false);
      return;
    }

    const req: CreateCargaisonRequest = {
      clientId: v.client!.id!,
      nombreColis: v.nombreColis,
      poidsKg: v.poidsKg ?? undefined,
      volumeM3: v.volumeM3 ?? undefined,
      montantTotal: v.montantTotal,
      montantRegle: v.montantRegle,
      devise: v.devise,
      observations: v.observations || undefined
    };

    this.cargaisons.createCargaison(req).subscribe({
      next: (c: any) => {
        this.toast.add({
          severity: 'success',
          summary: 'Cargaison créée',
          detail: `Numéro de traçage : ${c.numeroTracage}`
        });
        setTimeout(() => this.router.navigate(['/app/cargaisons']), 1200);
      },
      error: () => {
        this.toast.add({ severity: 'error', summary: 'Erreur', detail: 'Création impossible' });
        this.saving.set(false);
      }
    });
  }

  protected cancel(): void {
    this.router.navigate(['/app/cargaisons']);
  }

  private formatNomComplet(c: ClientDto): string {
    const tel = c.telephone ? ` (${c.telephone})` : '';
    return `${c.prenom ?? ''} ${c.nom ?? ''}${tel}`.trim();
  }
}
