import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Role } from '@core/models/role.enum';
import { AuthService } from './auth.service';

/**
 * Guard factory : seuls les rôles passés en argument peuvent activer la route.
 *
 * Usage :
 *   { path: 'navires', canActivate: [authGuard, roleGuard([Role.SUPERVISOR, Role.ADMIN])] }
 */
export const roleGuard = (allowed: Role[]): CanActivateFn => {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    return auth.hasRole(allowed) || router.createUrlTree(['/forbidden']);
  };
};
