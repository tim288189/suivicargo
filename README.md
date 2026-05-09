# Suivicargo — Backend

Application de gestion et de suivi de cargaison par fret maritime.

Backend Spring Boot exposant une API REST consommée par un front Angular (séparé). Authentification JWT, rôles (ADMIN / SUPERVISOR / EMPLOYEE), MySQL, Flyway, OpenAPI/Swagger.

## Stack technique

- Java 21 + Spring Boot 4.x
- Spring Web (MVC), Spring Data JPA, Spring Security, Spring Validation, Spring Mail
- MySQL 8 + Flyway pour les migrations
- JWT (jjwt 0.12)
- MapStruct pour le mapping DTO ↔ entités
- Lombok
- OpenHTMLtoPDF + FreeMarker pour la génération de factures PDF
- Springdoc OpenAPI 3 (Swagger UI)

## Démarrage en local

### 1. Prérequis

- JDK 21
- Maven 3.9+ (ou utiliser le wrapper `./mvnw`)
- Docker + Docker Compose

### 2. Lancer les dépendances (MySQL + MailDev)

```bash
docker compose up -d
```

Cela démarre :

- MySQL 8 sur le port `3306` (db `suivicargo`, user `suivicargo`, mdp `suivicargo`)
- MailDev (interception SMTP en dev) — Web UI : http://localhost:1080

> Spring Boot DevTools détecte aussi automatiquement `compose.yaml` au démarrage, ce qui peut lancer les services tout seul.

### 3. Lancer le backend

```bash
./mvnw spring-boot:run
```

L'application démarre sur http://localhost:8080/api avec le profil `dev` actif par défaut.

Au premier lancement, Flyway applique :

- `V1__init_schema.sql` — création des tables
- `V2__seed_admin.sql` — création de l'admin par défaut

### 4. Premier login

| Email | Mot de passe |
|---|---|
| admin@suivicargo.local | `admin123` |

> **Important** : changer ce mot de passe immédiatement après le premier login en production.

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@suivicargo.local","password":"admin123"}'
```

### 5. Documentation interactive

- Swagger UI : http://localhost:8080/api/swagger-ui.html
- OpenAPI JSON : http://localhost:8080/api/v3/api-docs

Le front Angular utilisera ce JSON OpenAPI pour générer automatiquement ses types et schémas de validation Zod via Orval.

## Structure du projet (architecture en couches)

```
src/main/java/com/elior/suivicargo/
├── SuivicargoApplication.java      # Point d'entrée Spring Boot
├── ServletInitializer.java         # Pour le packaging WAR
│
├── controllers/                    # Toutes les API REST
│   ├── AuthController.java
│   ├── CargaisonController.java
│   └── TrackingPublicController.java
│
├── services/                       # Toute la logique métier
│   ├── AppUserDetailsService.java
│   ├── AuthService.java
│   ├── CargaisonService.java
│   └── NumeroTracageService.java
│
├── repositories/                   # Tous les repositories Spring Data JPA
│   ├── UserRepository.java
│   ├── ClientRepository.java
│   ├── CargaisonRepository.java
│   ├── HistoriqueStatutRepository.java
│   ├── NavireRepository.java
│   ├── VoyageRepository.java
│   ├── ConteneurRepository.java
│   ├── PlanPaiementRepository.java
│   ├── EcheanceRepository.java
│   ├── ReglementRepository.java
│   └── FactureRepository.java
│
├── models/                         # Toutes les entités JPA
│   ├── BaseEntity.java             # Champs d'audit communs
│   ├── User.java
│   ├── Client.java
│   ├── Cargaison.java
│   ├── HistoriqueStatut.java
│   ├── Navire.java
│   ├── Voyage.java
│   ├── Conteneur.java
│   ├── PlanPaiement.java
│   ├── Echeance.java
│   ├── Reglement.java
│   └── Facture.java
│
├── dtos/                           # Tous les DTO (request, response, etc.)
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── AuthResponse.java
│   ├── CargaisonDto.java
│   ├── CreateCargaisonRequest.java
│   ├── TrackingPublicResponse.java
│   ├── PageResponse.java
│   └── ApiError.java
│
├── mappers/                        # Tous les mappers MapStruct
│   └── CargaisonMapper.java
│
├── enums/                          # Tous les enums métier
│   ├── Role.java
│   ├── StatutCargaison.java
│   ├── StatutPaiement.java
│   ├── StatutVoyage.java
│   └── ModePaiement.java
│
├── security/                       # Spring Security + JWT (gardé groupé pour cohésion)
│   ├── SecurityConfig.java         # Filter chain, CORS, @PreAuthorize
│   ├── JwtService.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtProperties.java
│   ├── CorsProperties.java
│   └── AppUserDetails.java
│
├── config/                         # Autres configurations
│   ├── JpaConfig.java              # Auditing JPA
│   └── OpenApiConfig.java          # Métadonnées OpenAPI + serveur du contrat statique
│
└── exceptions/                     # Gestion d'erreurs centralisée
    ├── BusinessException.java
    └── GlobalExceptionHandler.java

src/main/resources/
├── application.yaml                # Config commune
├── application-dev.yaml            # Profil dev (MySQL local, MailDev)
├── application-prod.yaml           # Profil prod (variables d'environnement)
├── db/migration/
│   ├── V1__init_schema.sql
│   └── V2__seed_admin.sql
└── openapi/
    └── openapi.yaml                # ⭐ Contrat OpenAPI source de vérité
```

## Contrat OpenAPI (source de vérité)

Le fichier `src/main/resources/openapi/openapi.yaml` est le **contrat d'interface** versionné dans le repo. Il décrit toute l'API : endpoints, schémas, sécurité, erreurs.

Trois façons d'y accéder :

| URL | Description |
|---|---|
| http://localhost:8080/api/openapi/openapi.yaml | Contrat statique (servi depuis le repo) |
| http://localhost:8080/api/v3/api-docs | OpenAPI auto-généré par Springdoc à partir du code |
| http://localhost:8080/api/swagger-ui.html | Interface interactive |

Pour le front Angular, on consommera le **contrat statique** avec **Orval** :

```ts
// orval.config.ts
export default {
  suivicargo: {
    input: 'http://localhost:8080/api/openapi/openapi.yaml',
    output: {
      target: './src/app/core/api/generated.ts',
      client: 'angular',
      schemas: './src/app/core/api/schemas',
      mode: 'tags-split',
    },
  },
  suivicargoZod: {
    input: 'http://localhost:8080/api/openapi/openapi.yaml',
    output: {
      target: './src/app/core/api/zod',
      client: 'zod',
    },
  },
};
```

## Modèle de rôles

| Action | Employé | Superviseur | Admin |
|---|---|---|---|
| Consulter cargaisons en cours | ✅ | ✅ | ✅ |
| Créer une cargaison à l'enlèvement | ✅ | ✅ | ✅ |
| Enregistrer un règlement (encaissement) | ✅ | ✅ | ✅ |
| Changer le statut d'une cargaison | ❌ | ✅ | ✅ |
| Gérer navires / voyages / conteneurs | ❌ | ✅ | ✅ |
| Définir un plan de paiement | ❌ | ✅ | ✅ |
| Inscrire / supprimer un utilisateur | ❌ | ❌ | ✅ |

## Endpoints principaux

### Public (sans authentification)

- `POST /api/v1/auth/login`
- `GET  /api/v1/tracking/{numeroTracage}` — suivi public

### Authentifié (JWT requis)

- `POST   /api/v1/auth/register` *(ADMIN)*
- `GET    /api/v1/cargaisons` — liste des cargaisons en cours
- `GET    /api/v1/cargaisons/{id}` — détail
- `POST   /api/v1/cargaisons` — création à l'enlèvement
- `PATCH  /api/v1/cargaisons/{id}/statut?statut=…` *(SUPERVISOR/ADMIN)*

## Génération du numéro de traçage

Format : `MAR-YYYY-NNNNNN` (ex: `MAR-2026-000042`).

Préfixe configurable via `app.tracking.prefix`. Séquence remise à zéro chaque année.

## Variables d'environnement (production)

| Variable | Description |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_URL` | URL JDBC MySQL |
| `DB_USER` / `DB_PASSWORD` | Identifiants MySQL |
| `APP_JWT_SECRET` | Secret JWT (Base64, ≥ 256 bits) |
| `APP_JWT_EXP_MIN` | Durée du token en minutes (défaut 120) |
| `MAIL_HOST` / `MAIL_PORT` / `MAIL_USER` / `MAIL_PASSWORD` | SMTP |
| `APP_CORS_ORIGINS` | Origines autorisées (CSV) |
| `TWILIO_SID` / `TWILIO_TOKEN` / `TWILIO_FROM` | WhatsApp via Twilio |

## Tests

```bash
./mvnw test
```

## Conventions

- **Pas d'entité JPA exposée dans les contrôleurs** — toujours via DTO.
- **Pagination** sur toutes les listes (`Pageable` Spring).
- **Validation** par Bean Validation (`@Valid`, `@NotNull`, etc.) — automatiquement reflétée dans l'OpenAPI.
- **Soft delete** via le flag `supprime` dans `BaseEntity`.
- **Migrations** Flyway versionnées, jamais modifier une migration déjà appliquée.
