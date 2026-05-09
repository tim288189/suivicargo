import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-public-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink],
  template: `
    <div class="min-h-screen flex flex-col">
      <header class="bg-white shadow-sm border-b border-gray-200">
        <div class="max-w-6xl mx-auto px-6 py-4 flex items-center justify-between">
          <a routerLink="/" class="text-xl font-bold" style="color:#0f4c81">SUIVICARGO</a>
          <a routerLink="/login" class="text-sm font-medium text-gray-700 hover:text-gray-900">
            Se connecter
          </a>
        </div>
      </header>
      <main class="flex-1">
        <router-outlet />
      </main>
      <footer class="bg-white border-t border-gray-200 py-4 text-center text-xs text-gray-500">
        &copy; {{year}} Suivicargo
      </footer>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PublicLayoutComponent {
  protected readonly year = new Date().getFullYear();
}
