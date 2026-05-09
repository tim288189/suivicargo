import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { AuthService } from '@core/auth/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  template: `
    <h1 class="text-2xl font-bold text-gray-900 mb-2">Tableau de bord</h1>
    @if (user()) {
      <p class="text-gray-600 mb-6">Bonjour {{ user()!.prenom }} {{ user()!.nom }} 👋</p>
    }
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div class="bg-white rounded-lg shadow p-5">
        <div class="text-xs uppercase text-gray-500">Cargaisons en cours</div>
        <div class="text-3xl font-bold text-[#0f4c81] mt-2">—</div>
      </div>
      <div class="bg-white rounded-lg shadow p-5">
        <div class="text-xs uppercase text-gray-500">À encaisser</div>
        <div class="text-3xl font-bold text-[#0f4c81] mt-2">—</div>
      </div>
      <div class="bg-white rounded-lg shadow p-5">
        <div class="text-xs uppercase text-gray-500">Livraisons cette semaine</div>
        <div class="text-3xl font-bold text-[#0f4c81] mt-2">—</div>
      </div>
    </div>
    <p class="text-xs text-gray-500 mt-8">
      Les KPI seront branchés sur l'API une fois le client Orval généré (npm run api:generate).
    </p>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardComponent {
  private readonly auth = inject(AuthService);
  protected readonly user = this.auth.currentUser;
}
