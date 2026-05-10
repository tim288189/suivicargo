import { Routes } from '@angular/router';
import { authGuard } from '@core/auth/auth.guard';
import { roleGuard } from '@core/auth/role.guard';
import { Role } from '@core/models/role.enum';

export const routes: Routes = [
  // Routes publiques
  {
    path: '',
    loadComponent: () => import('./layouts/public-layout.component').then(m => m.PublicLayoutComponent),
    children: [
      {
        path: 'login',
        loadComponent: () => import('./features/auth/login.component').then(m => m.LoginComponent)
      },
      {
        path: 'tracking/:numeroTracage',
        loadComponent: () =>
          import('./features/tracking/tracking-public.component').then(m => m.TrackingPublicComponent)
      },
      {
        path: 'forbidden',
        loadComponent: () => import('./features/errors/forbidden.component').then(m => m.ForbiddenComponent)
      }
    ]
  },

  // Routes authentifiées
  {
    path: 'app',
    canActivate: [authGuard],
    loadComponent: () => import('./layouts/main-layout.component').then(m => m.MainLayoutComponent),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'cargaisons',
        loadComponent: () =>
          import('./features/cargaisons/cargaisons-list.component').then(m => m.CargaisonsListComponent)
      },
      {
        path: 'cargaisons/new',
        loadComponent: () =>
          import('./features/cargaisons/cargaison-form.component').then(m => m.CargaisonFormComponent)
      },
      {
        path: 'cargaisons/:id',
        loadComponent: () =>
          import('./features/cargaisons/cargaison-detail.component').then(m => m.CargaisonDetailComponent)
      },
      {
        path: 'navires',
        canActivate: [roleGuard([Role.SUPERVISOR, Role.ADMIN])],
        loadComponent: () =>
          import('./features/navires/navires-list.component').then(m => m.NaviresListComponent)
      },
      {
        path: 'voyages',
        canActivate: [roleGuard([Role.EMPLOYEE, Role.SUPERVISOR, Role.ADMIN])],
        loadComponent: () =>
          import('./features/voyages/voyages-list.component').then(m => m.VoyagesListComponent)
      },
      {
        path: 'voyages/:id',
        canActivate: [roleGuard([Role.EMPLOYEE, Role.SUPERVISOR, Role.ADMIN])],
        loadComponent: () =>
          import('./features/voyages/voyage-detail.component').then(m => m.VoyageDetailComponent)
      },
      {
        path: 'admin/users',
        canActivate: [roleGuard([Role.ADMIN])],
        loadComponent: () =>
          import('./features/admin/users-list.component').then(m => m.UsersListComponent)
      }
    ]
  },

  { path: '**', redirectTo: '/login' }
];
