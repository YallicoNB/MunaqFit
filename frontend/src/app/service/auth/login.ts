import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RespuestaLogin } from '../../models/respuesta-login';
import { API_URL } from '../../core/api';

@Injectable({
  providedIn: 'root',
})
export class Login {
  private apiUrlLogin = `${API_URL}/auth/login`;

  constructor(private http: HttpClient) {}

  iniciarSesion(dni: string, password: string): Observable<RespuestaLogin> {
    return this.http.post<RespuestaLogin>(this.apiUrlLogin, { dni, password });
  }

  /**
   * Pide el endpoint de salud para que el backend empiece a despertar.
   *
   * El plan gratuito de Render suspende la instancia tras 15 minutos sin
   * trafico y tarda en volver a levantarse. Llamar a /api/health al abrir la
   * pagina hace que ese arranque ocurra mientras el usuario escribe sus
   * credenciales, en lugar de despues de pulsar el boton.
   *
   * No consulta la base de datos y no requiere autenticacion. Si falla no se
   * avisa al usuario: es una optimizacion silenciosa.
   */
  despertar(): void {
    this.http.get(`${API_URL}/health`).subscribe({ error: () => undefined });
  }
}
