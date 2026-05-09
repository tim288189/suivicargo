import { defineConfig } from 'orval';

/**
 * Génère depuis le contrat OpenAPI statique du backend :
 *   - Types TypeScript + services Angular (HttpClient + Observable)
 *   - Schémas Zod pour validation runtime
 *
 * À lancer : npm run api:generate
 *
 * Sources d'OpenAPI possibles :
 *   - Fichier local : '../src/main/resources/openapi/openapi.yaml'
 *   - Endpoint live : 'http://localhost:8080/api/openapi/openapi.yaml'
 *
 * Le fichier statique est plus stable (pas besoin que le back tourne).
 */
export default defineConfig({
  suivicargo: {
    input: '../src/main/resources/openapi/openapi.yaml',
    output: {
      mode: 'tags-split',
      target: './src/app/core/api/generated/api.ts',
      schemas: './src/app/core/api/generated/schemas',
      client: 'angular',
      clean: true,
      override: {
        useTypeOverInterfaces: true,
        useDates: true,
        angular: {
          provideIn: 'root'
        },
        mutator: {
          path: './src/app/core/api/api-mutator.ts',
          name: 'apiMutator'
        }
      }
    }
  },
  suivicargoZod: {
    input: '../src/main/resources/openapi/openapi.yaml',
    output: {
      mode: 'tags-split',
      target: './src/app/core/api/generated/zod',
      client: 'zod',
      fileExtension: '.zod.ts'
    }
  }
});
