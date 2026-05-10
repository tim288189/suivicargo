import { ChangeDetectionStrategy, Component, inject, Input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { DatePipe, DecimalPipe } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { SelectModule } from 'primeng/select';
import { InputTextModule } from 'primeng/inputtext';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';

import { VoyagesService } from '@api-gen/voyages/voyages.service';
import {
  CargaisonDto,
  StatutVoyage,
  VoyageDto
} from '@api-gen/schemas';
import { AuthService } from '@core/auth/auth.service';
import { Role } from '@core/models/role.enum';

@Component({
  selector: 'app-voyage-detail',
  standalone: true,
  imports: [
    DatePipe, DecimalPipe, RouterLink, ReactiveFormsModule,
    TableModule, ButtonModule, TagModule, DialogModule, SelectModule,
    InputTextModule, ToastModule
  ],
  providers: [MessageService],
  template: `
    <p-toast></p-toast>

    @if (loading()) {
      <div class="text-gray-500">Chargement…</div>
    } @else if (voyage(); as v) {

      <div class="flex flex-wrap items-start justify-between gap-3 mb-6">
        <div>
          <a routerLink="/app/voyages" class="text-sm text-gray-500 hover:text-gray-700">
            <i class="pi pi-arrow-left text-xs"></i> Tous les voyages
          </a>
          <h1 class="text-2xl font-bold text-gray-900 mt-2">{{ v.navireNom }}</h1>
          <div class="text-gray-700 mt-1">
            <i class="pi pi-arrow-right-arrow-left text-xs mr-1"></i>
            {{ v.portDepart }} → {{ v.portArrivee }}
          </div>
          <div class="text-sm text-gray-500 mt-1">
            Départ : {{ v.dateDepart | date: 'longDate' }}
            · ETA : {{ v.etaArrivee | date: 'longDate' }}
            @if (v.dateArriveeReelle) {
              · Arrivée réelle : {{ v.dateArriveeReelle | date: 'longDate' }}
            }
          </div>
          <div class="mt-3">
            <p-tag [value]="v.statut!" [severity]="severityFor(v.statut!)"></p-tag>
          </div>
        </div>

        @if (canChangeStatus()) {
          <button pButton icon="pi pi-sync" label="Changer le statut"
                  (click)="openStatusDialog()"></button>
        }
      </div>

      <!-- ===== Cargaisons embarquées ===== -->
      <section class="bg-white rounded-lg shadow p-5">
        <div class="flex items-center justify-between mb-3">
          <h2 class="text-sm font-semibold text-gray-500 uppercase tracking-wider">
            Cargaisons à bord
          </h2>
          <span class="text-sm text-gray-600">{{ cargaisons().length }} cargaison(s)</span>
        </div>

        <p-table [value]="cargaisons()" [loading]="loadingCargaisons()"
                 styleClass="p-datatable-sm p-datatable-striped">
          <ng-template pTemplate="header">
            <tr>
              <th>Tracking</th>
              <th>Client</th>
              <th>Colis</th>
              <th class="text-right">Total</th>
              <th>Statut</th>
            </tr>
          </ng-template>
          <ng-template pTemplate="body" let-c>
            <tr (click)="openCargaison(c.id)" class="cursor-pointer hover:bg-blue-50">
              <td class="font-mono text-blue-700">{{ c.numeroTracage }}</td>
              <td>{{ c.clientNomComplet }}</td>
              <td>{{ c.nombreColis }}</td>
              <td class="text-right">{{ c.montantTotal | number: '1.0-0' }} {{ c.devise }}</td>
              <td><p-tag [value]="c.statut" [severity]="cargoSeverity(c.statut)"></p-tag></td>
            </tr>
          </ng-template>
          <ng-template pTemplate="emptymessage">
            <tr>
              <td colspan="5" class="text-center text-gray-500 py-6">
                Aucune cargaison rattachée à ce voyage.
              </td>
            </tr>
          </ng-template>
        </p-table>
      </section>

      <!-- Dialog changement statut voyage -->
      <p-dialog [(visible)]="statusDialogVisible" header="Changer le statut du voyage"
                [modal]="true" [style]="{ width: '32rem' }" [closable]="!savingStatus()">
        <form [formGroup]="statusForm" (ngSubmit)="submitStatus()" class="flex flex-col gap-4">
          <div class="bg-blue-50 border-l-4 border-blue-400 p-3 text-sm">
            <strong>Impact :</strong> le nouveau statut sera automatiquement propagé
            aux <strong>{{ cargaisons().length }}</strong> cargaison(s) rattachée(s)
            (sauf celles déjà LIVRE ou ANNULE).
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Nouveau statut *</label>
            <p-select formControlName="statut" [options]="statutOptions"
                      [style]="{ width: '100%' }" placeholder="Sélectionner"></p-select>
          </div>

          <div class="flex justify-end gap-2 pt-2">
            <button pButton type="button" label="Annuler" class="p-button-text"
                    (click)="statusDialogVisible.set(false)"
                    [disabled]="savingStatus()"></button>
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
export class VoyageDetailComponent {

  private readonly api    = inject(VoyagesService);
  private readonly auth   = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb     = inject(FormBuilder);
  private readonly toast  = inject(MessageService);

  protected readonly voyage          = signal<VoyageDto | null>(null);
  protected readonly cargaisons      = signal<CargaisonDto[]>([]);
  protected readonly loading         = signal(true);
  protected readonly loadingCargaisons = signal(true);

  protected readonly statusDialogVisible = signal(false);
  protected readonly savingStatus        = signal(false);

  protected readonly statutOptions = [
    { label: 'Programmé',  value: 'PROGRAMME' },
    { label: 'En mer',     value: 'EN_MER' },
    { label: 'Arrivé',     value: 'ARRIVE' },
    { label: 'Annulé',     value: 'ANNULE' }
  ];

  protected readonly statusForm = this.fb.nonNullable.group({
    statut: ['' as StatutVoyage | '', Validators.required]
  });

  @Input() set id(value: string) {
    this.fetch(Number(value));
  }

  protected canChangeStatus(): boolean {
    return this.auth.hasRole([Role.SUPERVISOR, Role.ADMIN]);
  }

  protected severityFor(s: string): 'success' | 'info' | 'warn' | 'danger' | 'contrast' {
    switch (s) {
      case 'ARRIVE':    return 'success';
      case 'EN_MER':    return 'warn';
      case 'ANNULE':    return 'danger';
      case 'PROGRAMME': return 'info';
      default:          return 'contrast';
    }
  }

  protected cargoSeverity(s: string): 'success' | 'info' | 'warn' | 'danger' | 'contrast' {
    switch (s) {
      case 'LIVRE':              return 'success';
      case 'EN_DOUANE':
      case 'EN_MER':             return 'warn';
      case 'ANNULE':             return 'danger';
      case 'AU_PORT_ARRIVEE':    return 'info';
      default:                   return 'contrast';
    }
  }

  protected openCargaison(id: number): void {
    this.router.navigate(['/app/cargaisons', id]);
  }

  protected openStatusDialog(): void {
    this.statusForm.reset({ statut: this.voyage()?.statut ?? '' });
    this.statusDialogVisible.set(true);
  }

  protected submitStatus(): void {
    const v = this.voyage();
    const statut = this.statusForm.getRawValue().statut;
    if (!v || !statut) return;
    this.savingStatus.set(true);

    this.api.updateVoyage(v.id!, { statut } as never).subscribe({
      next: () => {
        this.toast.add({
          severity: 'success',
          summary: 'Statut mis à jour',
          detail: 'Cargaisons synchronisées'
        });
        this.statusDialogVisible.set(false);
        this.savingStatus.set(false);
        this.fetch(v.id!);
      },
      error: () => {
        this.toast.add({ severity: 'error', summary: 'Erreur', detail: 'Mise à jour impossible' });
        this.savingStatus.set(false);
      }
    });
  }

  private fetch(id: number): void {
    this.loading.set(true);
    this.loadingCargaisons.set(true);

    this.api.getVoyageById(id).subscribe({
      next: (res) => {
        this.voyage.set(res);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });

    this.api.listCargaisonsDuVoyage(id).subscribe({
      next: (cargaisons: any) => {
        this.cargaisons.set(cargaisons ?? []);
        this.loadingCargaisons.set(false);
      },
      error: () => this.loadingCargaisons.set(false)
    });
  }
}
