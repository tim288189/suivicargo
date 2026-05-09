import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-forbidden',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="max-w-md mx-auto mt-20 text-center">
      <div class="text-6xl mb-4">⛔</div>
      <h1 class="text-2xl font-bold mb-2">Accès refusé</h1>
      <p class="text-gray-600 mb-6">Vous n'avez pas les droits requis pour accéder à cette page.</p>
      <a routerLink="/app/dashboard" class="text-[#0f4c81] underline">Retour au tableau de bord</a>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ForbiddenComponent {}
