import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';

import { NaviresService } from '@api-gen/navires/navires.service';
import { CreateNavireRequest, NavireDto } from '@api-gen/schemas';

@Component({
  selector: 'app-navires-list',
  standalone: true,
  imports: [
    TableModule, ButtonModule, DialogModule, InputTextModule, InputNumberModule,
    ConfirmDialogModule, ToastModule, ReactiveFormsModule
  ],
  providers: [ConfirmationService, MessageService],
  template: `
    <p-toast></p-toast>
    <p-confirmDialog></p-confirmDialog>

    <div class="flex items-center justify-between mb-4">
      <h1 class="text-2xl font-bold text-gray-900">Navires</h1>
      <button pButton icon="pi pi-plus" label="Nouveau navire" (click)="openNew()"></button>
    </div>

    <p-table [value]="navires()" [loading]="loading()" [paginator]="true" [rows]="20"
             styleClass="p-datatable-sm p-datatable-striped">
      <ng-template pTemplate="header">
        <tr>
          <th>Nom</th>
          <th>IMO</th>
          <th>Pavillon</th>
          <th>Capacité (EVP)</th>
          <th></th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-n>
        <tr>
          <td class="font-semibold">{{ n.nom }}</td>
          <td class="font-mono">{{ n.imo }}</td>
          <td>{{ n.pavillon || '—' }}</td>
          <td>{{ n.capaciteEvp || '—' }}</td>
          <td class="text-right">
            <button pButton icon="pi pi-pencil" class="p-button-text p-button-sm" (click)="edit(n)"></button>
            <button pButton icon="pi pi-trash" class="p-button-text p-button-sm p-button-danger"
                    (click)="confirmDelete(n)"></button>
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr><td colspan="5" class="text-center text-gray-500 py-8">Aucun navire enregistré.</td></tr>
      </ng-template>
    </p-table>

    <!-- Dialogue création/édition -->
    <p-dialog [(visible)]="dialogVisible" [header]="editingId() ? 'Modifier le navire' : 'Nouveau navire'"
              [modal]="true" [style]="{ width: '32rem' }" [closable]="!saving()">
      <form [formGroup]="form" (ngSubmit)="save()" class="flex flex-col gap-4">
        <div>
          <label class="block text-sm font-medium mb-1">Nom *</label>
          <input pInputText formControlName="nom" class="w-full"/>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Numéro IMO * (7 chiffres)</label>
          <input pInputText formControlName="imo" class="w-full" maxlength="7"/>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Pavillon</label>
          <input pInputText formControlName="pavillon" class="w-full"/>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Capacité (EVP)</label>
          <p-inputNumber formControlName="capaciteEvp" [showButtons]="true"></p-inputNumber>
        </div>

        <div class="flex justify-end gap-2 pt-2">
          <button pButton type="button" label="Annuler" class="p-button-text"
                  (click)="dialogVisible.set(false)" [disabled]="saving()"></button>
          <button pButton type="submit" label="Enregistrer"
                  [disabled]="form.invalid || saving()" [loading]="saving()"></button>
        </div>
      </form>
    </p-dialog>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class NaviresListComponent {

  private readonly api  = inject(NaviresService);
  private readonly fb   = inject(FormBuilder);
  private readonly toast = inject(MessageService);
  private readonly confirm = inject(ConfirmationService);

  protected readonly navires = signal<NavireDto[]>([]);
  protected readonly loading = signal(true);
  protected readonly saving  = signal(false);
  protected readonly dialogVisible = signal(false);
  protected readonly editingId = signal<number | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    nom:         ['', [Validators.required, Validators.maxLength(150)]],
    imo:         ['', [Validators.required, Validators.pattern(/^\d{7}$/)]],
    pavillon:    [''],
    capaciteEvp: [null as number | null]
  });

  constructor() {
    this.fetch();
  }

  protected openNew(): void {
    this.editingId.set(null);
    this.form.reset();
    this.form.controls.imo.enable();
    this.dialogVisible.set(true);
  }

  protected edit(n: NavireDto): void {
    this.editingId.set(n.id!);
    this.form.patchValue({
      nom: n.nom,
      imo: n.imo,
      pavillon: n.pavillon ?? '',
      capaciteEvp: n.capaciteEvp ?? null
    });
    // L'IMO n'est plus modifiable une fois créé
    this.form.controls.imo.disable();
    this.dialogVisible.set(true);
  }

  protected save(): void {
    if (this.form.invalid) return;
    this.saving.set(true);
    const value = this.form.getRawValue();
    const editingId = this.editingId();

    const obs = editingId == null
      ? this.api.createNavire(value as CreateNavireRequest)
      : this.api.updateNavire(editingId, {
          nom: value.nom,
          pavillon: value.pavillon,
          capaciteEvp: value.capaciteEvp ?? undefined
        } as never);

    obs.subscribe({
      next: () => {
        this.toast.add({ severity: 'success', summary: 'Enregistré', detail: 'Navire sauvegardé' });
        this.dialogVisible.set(false);
        this.saving.set(false);
        this.fetch();
      },
      error: () => {
        this.toast.add({ severity: 'error', summary: 'Erreur', detail: 'Échec de l\'enregistrement' });
        this.saving.set(false);
      }
    });
  }

  protected confirmDelete(n: NavireDto): void {
    this.confirm.confirm({
      message: `Supprimer le navire « ${n.nom} » ?`,
      header: 'Confirmation',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Supprimer',
      rejectLabel: 'Annuler',
      accept: () => this.delete(n.id!)
    });
  }

  private delete(id: number): void {
    this.api.deleteNavire(id).subscribe({
      next: () => {
        this.toast.add({ severity: 'success', summary: 'Supprimé', detail: 'Navire supprimé' });
        this.fetch();
      },
      error: () => this.toast.add({ severity: 'error', summary: 'Erreur', detail: 'Suppression impossible' })
    });
  }

  private fetch(): void {
    this.loading.set(true);
    this.api.listNavires({ size: 100 } as never).subscribe({
      next: (page: any) => {
        this.navires.set(page.content ?? []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }
}
