import { inject } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { Router } from '@angular/router';
import {
  Observable,
  catchError,
  finalize,
  shareReplay,
  switchMap,
  tap,
  throwError,
} from 'rxjs';
import { API_URL } from '../core/api';
import { RespuestaLogin } from '../models/respuesta-login';
import { Token } from '../service/auth/token';

/**
 * Un unico refresco compartido: si cinco peticiones reciben 401 a la vez,
 * solo se llama una vez a /api/auth/refresh y todas reintentan con el
 * token nuevo (evita la estampida de refresh tokens rotados).
 */
let refrescoEnCurso: Observable<RespuestaLogin> | null = null;

/**
 * Agrega el header Authorization a cada llamada a la API.
 *
 * Si el backend responde 401 porque el access token (15 min) vencio, usa el
 * refresh token (7 dias) para pedir uno nuevo y reintenta la peticion
 * original sin que el usuario pierda la sesion. Si el refresh tambien fallo,
 * limpia ambos tokens y manda al login.
 *
 * Se registra en app.config.ts con provideHttpClient(withInterceptors([...])).
 */
export const AuthInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(Token);
  const router = inject(Router);
  const http = inject(HttpClient);

  const esLogin = req.url.includes('/api/auth/login');
  const esRefresh = req.url.includes('/api/auth/refresh');
  const esPublico =
    esLogin ||
    esRefresh ||
    req.url.includes('/api/auth/forgot-password') ||
    req.url.includes('/api/auth/reset-password');

  let request = req;
  const jwt = token.getToken();
  if (!esPublico && jwt) {
    request = req.clone({
      setHeaders: { Authorization: `Bearer ${jwt}` },
    });
  }

  const cerrarSesionYVolver = () => {
    token.cerrarSesion();
    router.navigate(['/']);
  };

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status !== 401 || esPublico) {
        return throwError(() => error);
      }

      const refresh = token.getRefreshToken();
      if (!refresh) {
        cerrarSesionYVolver();
        return throwError(() => error);
      }

      let refresco = refrescoEnCurso;
      if (!refresco) {
        refresco = http
          .post<RespuestaLogin>(`${API_URL}/auth/refresh`, { refreshToken: refresh })
          .pipe(
            tap((respuesta) => token.guardarSesion(respuesta)),
            finalize(() => {
              refrescoEnCurso = null;
            }),
            shareReplay({ bufferSize: 1, refCount: false })
          );
        refrescoEnCurso = refresco;
      }

      return refresco.pipe(
        switchMap((respuesta) =>
          next(
            req.clone({
              setHeaders: { Authorization: `Bearer ${respuesta.token}` },
            })
          )
        ),
        catchError((err) => {
          cerrarSesionYVolver();
          return throwError(() => err);
        })
      );
    })
  );
};
