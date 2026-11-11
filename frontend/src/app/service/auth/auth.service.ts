import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError, map, of } from 'rxjs';
import { API_URL } from '../../core/api';
import { RespuestaLogin } from '../../models/respuesta-login';
import { Token } from './token';

export interface RespuestaValidarToken {
  valido: boolean;
  dni: string;
  rol: string;
}

/**
 * Envuelve al servicio Login que ya existia y le agrega la validacion
 * del token contra el backend.
 */
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = `${API_URL}/auth`;

  constructor(
    private http: HttpClient,
    private token: Token
  ) { }

  /**
   * Le pregunta al backend si el token guardado sigue siendo valido.
   * Devuelve 200 si lo es y 401 si no.
   */
  validarToken(): Observable<RespuestaValidarToken> {
    return this.http.get<RespuestaValidarToken>(`${this.apiUrl}/validar-token`);
  }

  /**
   * Revisa la sesion al arrancar la app. Si el token ya no sirve,
   * lo limpia y devuelve false.
   */
  comprobarSesion(): Observable<boolean> {
    return this.validarToken().pipe(
      map((r) => r.valido),
      catchError(() => {
        this.token.cerrarSesion();
        return of(false);
      })
    );
  }

  recuperarPassword(correo: string) {
    return this.http.post(`${this.apiUrl}/forgot-password`, { email: correo });
  }

  resetearPassword(token: string, nuevaPassword: string) {
    return this.http.post(`${this.apiUrl}/reset-password`, { token, newPassword: nuevaPassword });
  }

  /**
   * Canjea el refresh token (7 dias) por un access token nuevo. El backend
   * tambien rota el refresh. Lo usa el interceptor cuando una peticion
   * responde 401 por access token vencido.
   */
  refrescarToken(refreshToken: string) {
    return this.http.post<RespuestaLogin>(`${this.apiUrl}/refresh`, { refreshToken });
  }

  cerrarSesion(): void {
    this.token.cerrarSesion();
  }
}
