import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '@core/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="max-w-md mx-auto mt-16 bg-white rounded-lg shadow p-8">
      <h1 class="text-2xl font-bold mb-6 text-gray-900">Connexion</h1>

      <form [formGroup]="form" (ngSubmit)="submit()" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <input type="email" formControlName="email"
                 class="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-[#0f4c81]" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Mot de passe</label>
          <input type="password" formControlName="password"
                 class="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-[#0f4c81]" />
        </div>

        @if (errorMessage()) {
          <div class="text-sm text-red-600 bg-red-50 border border-red-200 rounded px-3 py-2">
            {{ errorMessage() }}
          </div>
        }

        <button type="submit"
                [disabled]="form.invalid || loading()"
                class="w-full bg-[#0f4c81] text-white py-2 rounded hover:bg-[#0c3e6a] disabled:opacity-50">
          {{ loading() ? 'Connexion…' : 'Se connecter' }}
        </button>
      </form>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoginComponent {

  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    email:    ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  protected submit(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    this.errorMessage.set(null);

    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => this.router.navigate(['/app/dashboard']),
      error: (err: HttpErrorResponse) => {
        this.errorMessage.set(
          err.status === 401 ? 'Email ou mot de passe incorrect.' : 'Une erreur est survenue.'
        );
        this.loading.set(false);
      },
      complete: () => this.loading.set(false)
    });
  }
}
