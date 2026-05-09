import { Directive, effect, inject, Input, TemplateRef, ViewContainerRef } from '@angular/core';
import { Role } from '@core/models/role.enum';
import { AuthService } from '@core/auth/auth.service';

/**
 * Affiche le bloc seulement si l'utilisateur connecté a au moins un des rôles attendus.
 *
 * Usage :
 *   <button *appHasRole="['SUPERVISOR','ADMIN']">Nouveau navire</button>
 */
@Directive({
  selector: '[appHasRole]',
  standalone: true
})
export class HasRoleDirective {

  private readonly tpl = inject(TemplateRef<unknown>);
  private readonly vcr = inject(ViewContainerRef);
  private readonly auth = inject(AuthService);

  private allowed: Role[] = [];

  constructor() {
    effect(() => {
      // Re-évalue dès que currentUser() change.
      this.auth.currentUser();
      this.render();
    });
  }

  @Input() set appHasRole(roles: Role[] | Role) {
    this.allowed = Array.isArray(roles) ? roles : [roles];
    this.render();
  }

  private render(): void {
    this.vcr.clear();
    if (this.auth.hasRole(this.allowed)) {
      this.vcr.createEmbeddedView(this.tpl);
    }
  }
}
