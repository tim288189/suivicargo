import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { DatePipe, DecimalPipe } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';

import { CargaisonsService } from '@api-gen/cargaisons/cargaisons.service';
import { CargaisonDto } from '@api-gen/schemas';

@Component({
  selector: 'app-cargaisons-list',
  standalone: true,
  imports: [TableModule, ButtonModule, TagModule, DatePipe, DecimalPipe],
  template: `
    <div class="flex items-center justify-between mb-4">
      <h1 class="text-2xl font-bold text-gray-900">Cargaisons en cours</h1>
      <button pButton type="button" icon="pi pi-plus" label="Nouvelle cargaison"
              (click)="goToNew()"></button>
    </div>

    <p-table [value]="items()" [loading]="loading()" [paginator]="true" [rows]="20"
             [tableStyle]="{ 'min-width': '60rem' }"
             styleClass="p-datatable-sm p-datatable-striped clickable-rows">
      <ng-template pTemplate="header">
        <tr>
          <th>Tracking</th>
          <th>Client</th>
          <th>Colis</th>
          <th class="text-right">Total</th>
          <th class="text-right">Reste</th>
          <th>Enlèvement</th>
          <th>ETA livraison</th>
          <th>Statut</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-c>
        <tr (click)="open(c.id)" class="cursor-pointer hover:bg-blue-50">
          <td class="font-mono text-blue-700">{{ c.numeroTracage }}</td>
          <td>{{ c.clientNomComplet }}</td>
          <td>{{ c.nombreColis }}</td>
          <td class="text-right">{{ c.montantTotal | number: '1.0-0' }} {{ c.devise }}</td>
          <td class="text-right" [class.text-red-600]="c.montantRestant > 0">
            {{ c.montantRestant | number: '1.0-0' }} {{ c.devise }}
          </td>
          <td>{{ c.dateEnlevement | date: 'shortDate' }}</td>
          <td [class.text-red-600]="isEnRetard(c)">
            {{ c.dateLivraisonEstimee | date: 'shortDate' }}
          </td>
          <td>
            <p-tag [value]="c.statut" [severity]="severityFor(c.statut)"></p-tag>
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr>
          <td colspan="8" class="text-center text-gray-500 py-8">
            Aucune cargaison en cours.
          </td>
        </tr>
      </ng-template>
    </p-table>
  `,
  styles: [`
    :host ::ng-deep .clickable-rows tbody tr {
      transition: background-color 0.12s ease;
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CargaisonsListComponent {

  private readonly api = inject(CargaisonsService);
  private readonly router = inject(Router);

  protected readonly items = signal<CargaisonDto[]>([]);
  protected readonly loading = signal(true);

  constructor() {
    this.api.listCargaisonsEnCours({ size: 50 } as never).subscribe({
      next: (page: any) => {
        this.items.set(page.content ?? []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  protected open(id: number): void {
    this.router.navigate(['/app/cargaisons', id]);
  }

  protected goToNew(): void {
    this.router.navigate(['/app/cargaisons/new']);
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

  protected isEnRetard(c: CargaisonDto): boolean {
    if (!c.dateLivraisonEstimee || c.statut === 'LIVRE' || c.statut === 'ANNULE') {
      return false;
    }
    return new Date(c.dateLivraisonEstimee as unknown as string).getTime() < Date.now();
  }
}
