import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { Role } from '@core/models/role.enum';
import { StoredUser, TokenService } from './token.service';

// Service typé généré par Orval depuis le tag "Auth" de l'OpenAPI.
// IMPORTANT : lancer `npm run api:generate` au moins une fois pour que ces imports résolvent.
import { AuthService as GeneratedAuthService } from '@api-gen/auth/auth.service';
import { AuthResponse, LoginRequest, RegisterRequest } from '@api-gen/schemas';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly router = inject(Router);
  private readonly tokenService = inject(TokenService);
  private readonly api = inject(GeneratedAuthService);

  // Source de vérité réactive de l'utilisateur courant.
  private readonly _currentUser = signal<StoredUser | null>(this.tokenService.getUser());

  readonly currentUser     = this._currentUser.asReadonly();
  readonly isAuthenticated = computed(() => this._currentUser() !== null);
  readonly currentRole     = computed(() => this._currentUser()?.role as Role | undefined);

  login(req: LoginRequest): Observable<AuthResponse> {
    return this.api.login(req).pipe(tap(res => this.persistAndSet(res)));
  }

  register(req: RegisterRequest): Observable<AuthResponse> {
    return this.api.register(req).pipe(tap(res => this.persistAndSet(res)));
  }

  logout(): void {
    this.tokenService.clear();
    this._currentUser.set(null);
    this.router.navigate(['/login']);
  }

  hasRole(roles: Role[] | Role): boolean {
    const role = this.currentRole();
    if (!role) return false;
    const list = Array.isArray(roles) ? roles : [roles];
    return list.includes(role);
  }

  private persistAndSet(res: AuthResponse): void {
    this.tokenService.setToken(res.token);
    this.tokenService.setUser(res.user as StoredUser);
    this._currentUser.set(res.user as StoredUser);
  }
}
