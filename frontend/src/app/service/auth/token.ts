import { Injectable } from '@angular/core';
import { jwtDecode } from 'jwt-decode';
import { RespuestaLogin } from '../../models/respuesta-login';

interface TokenPayload {
  sub?: string;
  rol?: string;
  role?: string;
  tipo?: string;
  exp?: number;
}

@Injectable({
  providedIn: 'root',
})
export class Token {
  private readonly TOKEN_KEY = 'token';
  private readonly REFRESH_KEY = 'refreshToken';
  private readonly NOMBRE_KEY = 'nombreUsuario';
  private readonly ROL_KEY = 'rolUsuario';
  private readonly DNI_KEY = 'dniUsuario';
  private readonly EMAIL_KEY = 'emailUsuario';

  guardarSesion(respuesta: RespuestaLogin): void {
    localStorage.setItem(this.TOKEN_KEY, respuesta.token);
    if (respuesta.refreshToken) {
      localStorage.setItem(this.REFRESH_KEY, respuesta.refreshToken);
    }
    localStorage.setItem(this.NOMBRE_KEY, respuesta.nombreCompleto);
    localStorage.setItem(this.ROL_KEY, respuesta.rol);
    localStorage.setItem(this.DNI_KEY, respuesta.dni);
    localStorage.setItem(this.EMAIL_KEY, respuesta.email);
  }

  /** Payload del access token solo si sigue vigente. */
  decodificarToken(): TokenPayload | null {
    const token = localStorage.getItem(this.TOKEN_KEY);
    if (!token) return null;

    try {
      const decoded = jwtDecode<TokenPayload>(token);
      const ahora = Date.now() / 1000;
      if (decoded.exp && decoded.exp < ahora) {
        return null;
      }
      return decoded;
    } catch {
      return null;
    }
  }

  /**
   * Payload aunque el access token este vencido. Se usa solo para leer el
   * rol mientras el interceptor renueva el token en la primera peticion.
   */
  decodificarPayload(): TokenPayload | null {
    const token = localStorage.getItem(this.TOKEN_KEY);
    if (!token) return null;
    try {
      return jwtDecode<TokenPayload>(token);
    } catch {
      return null;
    }
  }

  /** JWT crudo guardado. Lo usa EventSource (alertas SSE), que no puede
   *  mandar headers Authorization y lleva el token en la URL. */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_KEY);
  }

  /**
   * Hay sesion utilizable si el access token esta vigente o si existe un
   * refresh token que aun no expiro (el interceptor lo canjea por uno nuevo).
   */
  haySesion(): boolean {
    if (this.decodificarToken()) {
      return true;
    }
    const refresh = this.getRefreshToken();
    if (!refresh) {
      return false;
    }
    try {
      const decoded = jwtDecode<TokenPayload>(refresh);
      return !decoded.exp || decoded.exp >= Date.now() / 1000;
    } catch {
      return false;
    }
  }

  getRol(): string | null {
    const payload = this.decodificarPayload();
    if (payload?.role) return payload.role;
    if (payload?.rol) return payload.rol;
    return localStorage.getItem(this.ROL_KEY);
  }

  getNombre(): string | null {
    return localStorage.getItem(this.NOMBRE_KEY);
  }

  getDni(): string | null {
    return localStorage.getItem(this.DNI_KEY);
  }

  getEmail(): string | null {
    return localStorage.getItem(this.EMAIL_KEY);
  }

  cerrarSesion(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_KEY);
    localStorage.removeItem(this.NOMBRE_KEY);
    localStorage.removeItem(this.ROL_KEY);
    localStorage.removeItem(this.DNI_KEY);
    localStorage.removeItem(this.EMAIL_KEY);
  }
}
