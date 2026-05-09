# Suivicargo — Frontend (Angular)

Application Angular 17 (standalone components, signals, control flow `@if`/`@for`) consommant l'API du backend Spring Boot.

## Stack

- **Angular 17+** (standalone, signals)
- **PrimeNG 17** + **PrimeIcons** + **PrimeFlex**
- **Tailwind CSS 3**
- **Orval** pour générer types TypeScript + client Angular + schémas Zod depuis l'OpenAPI
- **Zod** pour la validation runtime

## Prérequis

- **Node.js LTS** (22 ou 24). Le projet est testé sur Node 24 LTS.
  Les fichiers `.nvmrc` et `.node-version` imposent la version.

```bash
# Vérifier ta version
node --version    # doit afficher v22.x ou v24.x
npm --version     # doit être >= 10
```

> Ne pas utiliser Node 25 (Current) : Spectral (validateur OpenAPI utilisé par Orval)
> y déclenche un `SyntaxError` à cause d'AJV qui génère du JS incompatible avec V8 récent.

## Installation

```bash
cd frontend
npm install
```

## Génération du client API depuis l'OpenAPI

Le backend expose son contrat en `src/main/resources/openapi/openapi.yaml`. Le `orval.config.ts` est branché dessus.

```bash
npm run api:generate
```

Cette commande crée :

- `src/app/core/api/generated/` — services Angular typés (un fichier par tag OpenAPI)
- `src/app/core/api/generated/zod/` — schémas Zod pour validation runtime

> Le dossier `generated/` est dans le `.gitignore` — il est **régénéré automatiquement** au démarrage (`prestart`) et au build (`prebuild`).

## Démarrage

```bash
npm start
```

L'app tourne sur http://localhost:4200. Elle attend l'API sur http://localhost:8080/api (configurable dans `src/environments/environment.ts`).

## Compte par défaut

| Email | Mot de passe |
|---|---|
| admin@suivicargo.local | `admin123` |

## Structure

```
src/app/
├── core/
│   ├── api/
│   │   ├── api-mutator.ts          # Wrapper HttpClient pour Orval
│   │   └── generated/              # ⚙️ Auto-généré par Orval (gitignore)
│   ├── auth/
│   │   ├── auth.service.ts         # Login, état utilisateur (signal)
│   │   ├── token.service.ts        # localStorage token + user
│   │   ├── auth.interceptor.ts     # Ajoute le JWT aux requêtes
│   │   ├── error.interceptor.ts    # 401/403 → redirections
│   │   ├── auth.guard.ts           # Bloque si non authentifié
│   │   └── role.guard.ts           # Bloque selon le rôle
│   └── models/
│       └── role.enum.ts
├── shared/
│   └── directives/
│       └── has-role.directive.ts   # *appHasRole="['ADMIN']"
├── layouts/
│   ├── public-layout.component.ts  # Pages publiques (login, tracking)
│   └── main-layout.component.ts    # Sidebar + header (utilisateurs connectés)
├── features/
│   ├── auth/login.component.ts
│   ├── dashboard/dashboard.component.ts
│   ├── cargaisons/cargaisons-list.component.ts
│   ├── tracking/tracking-public.component.ts
│   ├── navires/navires-list.component.ts        (stub)
│   ├── admin/users-list.component.ts            (stub)
│   └── errors/forbidden.component.ts
├── app.config.ts                   # providers (router, http, interceptors)
├── app.routes.ts                   # Routes avec guards par rôle
└── app.component.ts                # <router-outlet>
```

## Convention : utiliser Orval plutôt que HttpClient direct

Une fois `npm run api:generate` lancé, remplacer les appels HttpClient bruts par les services générés :

```ts
// AVANT (HttpClient brut)
this.http.get(`${env.apiUrl}/v1/cargaisons`).subscribe(...);

// APRÈS (service typé Orval)
this.cargaisonsService.listCargaisonsEnCours({ page: 0, size: 20 }).subscribe(...);
```

Bénéfices :
- Types TypeScript stricts auto-synchronisés avec le back
- Validation runtime via les schémas Zod générés
- Aucune duplication de DTO

## Routes

| Path | Auth | Description |
|---|---|---|
| `/login` | publique | Connexion |
| `/tracking/:numeroTracage` | publique | Suivi par numéro de traçage |
| `/forbidden` | publique | Accès refusé |
| `/app/dashboard` | tous rôles | Tableau de bord |
| `/app/cargaisons` | tous rôles | Liste cargaisons en cours |
| `/app/navires` | SUPERVISOR/ADMIN | Gestion des navires |
| `/app/admin/users` | ADMIN | Gestion des utilisateurs |
