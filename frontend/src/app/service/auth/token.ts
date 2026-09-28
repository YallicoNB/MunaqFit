import { Injectable } from '@angular/core';
import { jwtDecode } from 'jwt-decode';
import { RespuestaLogin } from '../../models/respuesta-login';

interface TokenPayload {
  sub?: string;
  rol?: string;
  role?: string;
  exp?: number;
}

@Injectable({
  providedIn: 'root',
})
export class Token {
  private readonly TOKEN_KEY = 'token';
  private readonly NOMBRE_KEY = 'nombreUsuario';
  private readonly ROL_KEY = 'rolUsuario';
  private readonly DNI_KEY = 'dniUsuario';
  private readonly EMAIL_KEY = 'emailUsuario';

  guardarSesion(respuesta: RespuestaLogin): void {
    localStorage.setItem(this.TOKEN_KEY, respuesta.token);
    localStorage.setItem(this.NOMBRE_KEY, respuesta.nombreCompleto);
    localStorage.setItem(this.ROL_KEY, respuesta.rol);
    localStorage.setItem(this.DNI_KEY, respuesta.dni);
    localStorage.setItem(this.EMAIL_KEY, respuesta.email);
  }

  decodificarToken(): TokenPayload | null {
    const token = localStorage.getItem(this.TOKEN_KEY);
    if (!token) return null;

    try {
      const decoded = jwtDecode<TokenPayload>(token);
      const ahora = Date.now() / 1000;
      if (decoded.exp && decoded.exp < ahora) {
        this.cerrarSesion();
        return null;
      }
      return decoded;
    } catch {
      this.cerrarSesion();
      return null;
    }
  }

  getRol(): string | null {
    const payload = this.decodificarToken();
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
    localStorage.removeItem(this.NOMBRE_KEY);
    localStorage.removeItem(this.ROL_KEY);
    localStorage.removeItem(this.DNI_KEY);
    localStorage.removeItem(this.EMAIL_KEY);
  }
}