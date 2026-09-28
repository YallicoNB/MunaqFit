import { inject } from '@angular/core';
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { Token } from '../service/auth/token';

/**
 * Agrega el header Authorization a cada llamada a la API y, si el backend
 * responde 401, cierra la sesion y manda al login.
 *
 * Se registra en app.config.ts con provideHttpClient(withInterceptors([...])).
 */
export const AuthInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(Token);
  const router = inject(Router);

  const esPublico = req.url.includes('/api/auth/login');

  let request = req;
  if (!esPublico) {
    const jwt = localStorage.getItem('token');
    if (jwt) {
      request = req.clone({
        setHeaders: { Authorization: `Bearer ${jwt}` },
      });
    }
  }

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !esPublico) {
        token.cerrarSesion();
        router.navigate(['/']);
      }
      return throwError(() => error);
    })
  );
};
