import { HttpClient, HttpContext, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';

/**
 * Forme de requête attendue par Orval (client="angular").
 */
export interface ApiRequest<TBody = unknown> {
  url: string;
  method: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE' | 'OPTIONS' | 'HEAD'
        | 'get' | 'post' | 'put' | 'patch' | 'delete' | 'options' | 'head';
  params?: HttpParams | Record<string, string | number | boolean | ReadonlyArray<string | number | boolean>>;
  data?: TBody;
  headers?: HttpHeaders | Record<string, string | string[]>;
  responseType?: 'json' | 'blob' | 'text' | 'arraybuffer';
  withCredentials?: boolean;
  context?: HttpContext;
  signal?: AbortSignal;
}

/**
 * Mutator Orval — Angular client.
 *
 * Orval appelle <code>apiMutator(config, httpClient)</code> avec :
 *  1. la config de la requête (url, method, headers, body, params...)
 *  2. l'instance HttpClient injectée dans le service généré
 *
 * On préfixe la baseUrl de l'environnement et on délègue à HttpClient.
 * Le JWT est ajouté en transparence par AuthInterceptor.
 */
export const apiMutator = <T>(config: ApiRequest, http: HttpClient): Observable<T> => {
  return http.request<T>(config.method.toUpperCase(), `${environment.apiUrl}${config.url}`, {
    params: config.params as HttpParams,
    body: config.data,
    headers: config.headers as HttpHeaders,
    responseType: (config.responseType ?? 'json') as 'json',
    withCredentials: config.withCredentials,
    context: config.context
  });
};

export default apiMutator;
