import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RespuestaLogin } from '../../models/respuesta-login';

@Injectable({
  providedIn: 'root',
})
export class Login {
  private apiUrl = 'xxxxxx';

  constructor(private http: HttpClient) {}

  iniciarSesion(dni: string, password: string): Observable<RespuestaLogin> {
    return this.http.post<RespuestaLogin>(this.apiUrl, { dni, password });
  }
}