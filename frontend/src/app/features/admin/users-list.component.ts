import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { TagModule } from 'primeng/tag';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';

import { UsersService } from '@api-gen/users/users.service';
import { Role as ApiRole, UserDto } from '@api-gen/schemas';
import { AuthService } from '@core/auth/auth.service';

@Component({
  selector: 'app-users-list',
  standalone: true,
  imports: [
    TableModule, ButtonModule, DialogModule, InputTextModule, SelectModule, TagModule,
    ConfirmDialogModule, ToastModule, ReactiveFormsModule
  ],
  providers: [ConfirmationService, MessageService],
  template: `
    <p-toast></p-toast>
    <p-confirmDialog></p-confirmDialog>

    <div class="flex items-center justify-between mb-4">
      <h1 class="text-2xl font-bold text-gray-900">Utilisateurs</h1>
      <button pButton icon="pi pi-plus" label="Inscrire un employé" (click)="openNew()"></button>
    </div>

    <p-table [value]="users()" [loading]="loading()" [paginator]="true" [rows]="20"
             styleClass="p-datatable-sm p-datatable-striped">
      <ng-template pTemplate="header">
        <tr>
          <th>Nom complet</th>
          <th>Email</th>
          <th>Rôle</th>
          <th>État</th>
          <th></th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-u>
        <tr>
          <td>{{ u.prenom }} {{ u.nom }}</td>
          <td class="font-mono text-sm">{{ u.email }}</td>
          <td><p-tag [value]="u.role" [severity]="severityFor(u.role)"></p-tag></td>
          <td>
            @if (u.actif) {
              <p-tag value="Actif" severity="success"></p-tag>
            } @else {
              <p-tag value="Inactif" severity="danger"></p-tag>
            }
          </td>
          <td class="text-right">
            <button pButton icon="pi pi-trash" class="p-button-text p-button-sm p-button-danger"
                    [disabled]="u.role === 'ADMIN'"
                    (click)="confirmDelete(u)"></button>
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr><td colspan="5" class="text-center text-gray-500 py-8">Aucun utilisateur.</td></tr>
      </ng-template>
    </p-table>

    <p-dialog [(visible)]="dialogVisible" header="Inscrire un nouvel employé"
              [modal]="true" [style]="{ width: '32rem' }" [closable]="!saving()">
      <form [formGroup]="form" (ngSubmit)="save()" class="flex flex-col gap-4">
        <div>
          <label class="block text-sm font-medium mb-1">Email *</label>
          <input pInputText type="email" formControlName="email" class="w-full"/>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Mot de passe initial * (min 8 caractères)</label>
          <input pInputText type="password" formControlName="password" class="w-full"/>
        </div>
        <div class="grid grid-cols-2 gap-2">
          <div>
            <label class="block text-sm font-medium mb-1">Prénom *</label>
            <input pInputText formControlName="prenom" class="w-full"/>
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Nom *</label>
            <input pInputText formControlName="nom" class="w-full"/>
          </div>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Téléphone</label>
          <input pInputText formControlName="telephone" class="w-full"/>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Rôle *</label>
          <p-select formControlName="role" [options]="roleOptions"
                    placeholder="Sélectionner un rôle" [style]="{ width: '100%' }"></p-select>
        </div>

        <div class="flex justify-end gap-2 pt-2">
          <button pButton type="button" label="Annuler" class="p-button-text"
                  (click)="dialogVisible.set(false)" [disabled]="saving()"></button>
          <button pButton type="submit" label="Inscrire"
                  [disabled]="form.invalid || saving()" [loading]="saving()"></button>
        </div>
      </form>
    </p-dialog>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UsersListComponent {

  private readonly api = inject(UsersService);
  private readonly auth = inject(AuthService);
  private readonly fb   = inject(FormBuilder);
  private readonly toast = inject(MessageService);
  private readonly confirm = inject(ConfirmationService);

  protected readonly users   = signal<UserDto[]>([]);
  protected readonly loading = signal(true);
  protected readonly saving  = signal(false);
  protected readonly dialogVisible = signal(false);

  protected readonly roleOptions = [
    { label: 'Employé',     value: 'EMPLOYEE' },
    { label: 'Superviseur', value: 'SUPERVISOR' }
  ];

  protected readonly form = this.fb.nonNullable.group({
    email:     ['', [Validators.required, Validators.email]],
    password:  ['', [Validators.required, Validators.minLength(8)]],
    nom:       ['', Validators.required],
    prenom:    ['', Validators.required],
    telephone: [''],
    role:      ['EMPLOYEE' as ApiRole, Validators.required]
  });

  constructor() {
    this.fetch();
  }

  protected openNew(): void {
    this.form.reset({ role: 'EMPLOYEE' as ApiRole });
    this.dialogVisible.set(true);
  }

  protected save(): void {
    if (this.form.invalid) return;
    this.saving.set(true);
    // L'inscription d'un nouvel utilisateur passe par AuthService.register()
    this.auth.register(this.form.getRawValue() as never).subscribe({
      next: () => {
        this.toast.add({ severity: 'success', summary: 'Créé', detail: 'Utilisateur inscrit' });
        this.dialogVisible.set(false);
        this.saving.set(false);
        this.fetch();
      },
      error: () => {
        this.toast.add({ severity: 'error', summary: 'Erreur', detail: 'Inscription impossible' });
        this.saving.set(false);
      }
    });
  }

  protected confirmDelete(u: UserDto): void {
    this.confirm.confirm({
      message: `Désactiver le compte « ${u.email} » ?`,
      header: 'Confirmation',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Désactiver',
      rejectLabel: 'Annuler',
      accept: () => this.delete(u.id!)
    });
  }

  private delete(id: number): void {
    this.api.deleteUser(id).subscribe({
      next: () => {
        this.toast.add({ severity: 'success', summary: 'Désactivé', detail: 'Compte désactivé' });
        this.fetch();
      },
      error: () => this.toast.add({ severity: 'error', summary: 'Erreur', detail: 'Désactivation impossible' })
    });
  }

  protected severityFor(role: string): 'success' | 'info' | 'warn' {
    switch (role) {
      case 'ADMIN':      return 'warn';
      case 'SUPERVISOR': return 'info';
      default:           return 'success';
    }
  }

  private fetch(): void {
    this.loading.set(true);
    this.api.listUsers({ size: 100 } as never).subscribe({
      next: (page: any) => {
        this.users.set(page.content ?? []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }
}
