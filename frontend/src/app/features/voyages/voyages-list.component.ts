import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { DatePipe } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';

import { VoyagesService } from '@api-gen/voyages/voyages.service';
import { VoyageDto } from '@api-gen/schemas';

@Component({
  selector: 'app-voyages-list',
  standalone: true,
  imports: [TableModule, ButtonModule, TagModule, DatePipe],
  template: `
    <div class="flex items-center justify-between mb-4">
      <h1 class="text-2xl font-bold text-gray-900">Voyages</h1>
    </div>

    <p-table [value]="items()" [loading]="loading()" [paginator]="true" [rows]="20"
             styleClass="p-datatable-sm p-datatable-striped">
      <ng-template pTemplate="header">
        <tr>
          <th>Navire</th>
          <th>Trajet</th>
          <th>Départ</th>
          <th>ETA arrivée</th>
          <th>Arrivée réelle</th>
          <th>Statut</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-v>
        <tr (click)="open(v.id)" class="cursor-pointer hover:bg-blue-50">
          <td class="font-semibold">{{ v.navireNom }}</td>
          <td>{{ v.portDepart }} → {{ v.portArrivee }}</td>
          <td>{{ v.dateDepart | date: 'shortDate' }}</td>
          <td>{{ v.etaArrivee | date: 'shortDate' }}</td>
          <td>
            @if (v.dateArriveeReelle) {
              {{ v.dateArriveeReelle | date: 'shortDate' }}
            } @else {
              <span class="text-gray-400">—</span>
            }
          </td>
          <td>
            <p-tag [value]="v.statut" [severity]="severityFor(v.statut)"></p-tag>
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr>
          <td colspan="6" class="text-center text-gray-500 py-8">
            Aucun voyage enregistré.
          </td>
        </tr>
      </ng-template>
    </p-table>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class VoyagesListComponent {

  private readonly api = inject(VoyagesService);
  private readonly router = inject(Router);

  protected readonly items = signal<VoyageDto[]>([]);
  protected readonly loading = signal(true);

  constructor() {
    this.api.listVoyages({ size: 50 } as never).subscribe({
      next: (page: any) => {
        this.items.set(page.content ?? []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  protected open(id: number): void {
    this.router.navigate(['/app/voyages', id]);
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
}
