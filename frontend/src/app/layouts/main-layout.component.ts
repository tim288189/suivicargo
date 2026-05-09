import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '@core/auth/auth.service';
import { HasRoleDirective } from '@shared/directives/has-role.directive';
import { Role } from '@core/models/role.enum';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, HasRoleDirective],
  template: `
    <div class="min-h-screen flex bg-gray-50">

      <!-- ===== Sidebar ===== -->
      <aside class="w-64 bg-white border-r border-gray-200 flex flex-col shadow-sm">

        <!-- Logo -->
        <div class="h-16 flex items-center px-5 border-b border-gray-200">
          <a routerLink="/app/dashboard" class="flex items-center gap-2">
            <span class="inline-flex items-center justify-center w-8 h-8 rounded-md text-white font-bold"
                  style="background-color:#0f4c81">S</span>
            <span class="text-base font-bold text-gray-900">Suivicargo</span>
          </a>
        </div>

        <!-- Navigation -->
        <nav class="flex-1 px-3 py-4 space-y-1 text-sm">

          <a routerLink="/app/dashboard" routerLinkActive="nav-active"
             class="nav-link">
            <i class="pi pi-th-large"></i>
            <span>Tableau de bord</span>
          </a>

          <a routerLink="/app/cargaisons" routerLinkActive="nav-active"
             class="nav-link">
            <i class="pi pi-box"></i>
            <span>Cargaisons</span>
          </a>

          <a routerLink="/app/cargaisons/new" routerLinkActive="nav-active"
             class="nav-link">
            <i class="pi pi-plus-circle"></i>
            <span>Nouvelle cargaison</span>
          </a>

          <div *appHasRole="[Role.SUPERVISOR, Role.ADMIN]"
               class="pt-4 pb-1 px-3 text-[11px] font-semibold uppercase tracking-wider text-gray-400">
            Opérations
          </div>
          <a *appHasRole="[Role.SUPERVISOR, Role.ADMIN]"
             routerLink="/app/navires" routerLinkActive="nav-active"
             class="nav-link">
            <i class="pi pi-send"></i>
            <span>Navires</span>
          </a>

          <div *appHasRole="[Role.ADMIN]"
               class="pt-4 pb-1 px-3 text-[11px] font-semibold uppercase tracking-wider text-gray-400">
            Administration
          </div>
          <a *appHasRole="[Role.ADMIN]"
             routerLink="/app/admin/users" routerLinkActive="nav-active"
             class="nav-link">
            <i class="pi pi-users"></i>
            <span>Utilisateurs</span>
          </a>
        </nav>

        <!-- Profil utilisateur -->
        @if (user()) {
          <div class="border-t border-gray-200 px-4 py-3">
            <div class="flex items-center gap-3">
              <div class="inline-flex items-center justify-center w-9 h-9 rounded-full text-white text-sm font-semibold"
                   style="background-color:#0f4c81">
                {{ initials() }}
              </div>
              <div class="flex-1 min-w-0">
                <div class="text-sm font-semibold text-gray-900 truncate">
                  {{ user()!.prenom }} {{ user()!.nom }}
                </div>
                <div class="text-xs text-gray-500">{{ user()!.role }}</div>
              </div>
            </div>
            <button class="mt-3 w-full text-left text-sm text-gray-600 hover:text-gray-900 flex items-center gap-2"
                    (click)="logout()">
              <i class="pi pi-sign-out text-xs"></i>
              <span>Se déconnecter</span>
            </button>
          </div>
        }
      </aside>

      <!-- ===== Contenu principal ===== -->
      <main class="flex-1 overflow-x-hidden">
        <div class="px-8 py-6">
          <router-outlet />
        </div>
      </main>

    </div>
  `,
  styles: [`
    :host ::ng-deep .nav-link {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.55rem 0.75rem;
      border-radius: 0.375rem;
      color: #374151;          /* gray-700 */
      font-weight: 500;
      transition: background-color 0.15s ease, color 0.15s ease;
    }
    :host ::ng-deep .nav-link i {
      font-size: 0.95rem;
      color: #6b7280;          /* gray-500 */
      width: 1.1rem;
    }
    :host ::ng-deep .nav-link:hover {
      background-color: #f3f4f6;  /* gray-100 */
      color: #111827;             /* gray-900 */
    }
    :host ::ng-deep .nav-link:hover i {
      color: #0f4c81;
    }
    :host ::ng-deep .nav-link.nav-active {
      background-color: #eff6ff;  /* blue-50 */
      color: #0f4c81;
      font-weight: 600;
    }
    :host ::ng-deep .nav-link.nav-active i {
      color: #0f4c81;
    }
    :host ::ng-deep .nav-link.nav-active {
      box-shadow: inset 3px 0 0 0 #0f4c81;
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MainLayoutComponent {

  private readonly auth = inject(AuthService);

  protected readonly user = this.auth.currentUser;
  protected readonly Role = Role;

  protected initials(): string {
    const u = this.user();
    if (!u) return '';
    const a = u.prenom?.charAt(0) ?? '';
    const b = u.nom?.charAt(0) ?? '';
    return (a + b).toUpperCase();
  }

  protected logout(): void {
    this.auth.logout();
  }
}
