import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { TokenService } from './token.service';

/**
 * Intercepteur d'erreurs HTTP global :
 *  - 401 : token invalide/expiré → déconnexion + redirection /login
 *  - 403 : rôle insuffisant → redirection /forbidden
 *  - autres : laissé au caller
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const tokenService = inject(TokenService);

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401) {
        tokenService.clear();
        router.navigate(['/login']);
      } else if (err.status === 403) {
        router.navigate(['/forbidden']);
      }
      return throwError(() => err);
    })
  );
};
