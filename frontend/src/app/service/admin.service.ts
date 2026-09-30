import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../core/api';
import { ProductoInventario, ReabastecerRequest, Usuario, Proveedor, CrearUsuarioRequest } from '../models/admin';

@Injectable({
  providedIn: 'root',
})
export class AdminService {
  private urlInventario = `${API_URL}/admin/inventario`;
  private urlUsuarios = `${API_URL}/admin/usuarios`;
  private urlProveedores = `${API_URL}/admin/proveedores`;
  private urlVentas = `${API_URL}/admin/ventas`;

  constructor(private http: HttpClient) {}

  // ---- Inventario ----

  getInventario(): Observable<ProductoInventario[]> {
    return this.http.get<ProductoInventario[]>(this.urlInventario);
  }

  getStockCritico(): Observable<ProductoInventario[]> {
    return this.http.get<ProductoInventario[]>(`${this.urlInventario}/critico`);
  }

  getStockBajo(): Observable<ProductoInventario[]> {
    return this.http.get<ProductoInventario[]>(`${this.urlInventario}/bajo`);
  }

  reabastecer(datos: ReabastecerRequest): Observable<string> {
    return this.http.post<string>(`${this.urlInventario}/reabastecer`, datos);
  }

  // ---- Usuarios ----

  getUsuarios(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.urlUsuarios);
  }

  /** Crea un empleado nuevo. El backend siempre le asigna el rol EMPLEADO. */
  crearUsuario(usuario: CrearUsuarioRequest): Observable<Usuario> {
    return this.http.post<Usuario>(this.urlUsuarios, usuario);
  }

  /** Inactiva al empleado (soft delete). */
  eliminarUsuarioById(id: number): Observable<string> {
    return this.http.delete<string>(`${this.urlUsuarios}/${id}`);
  }

  // ---- Proveedores ----

  getProveedores(): Observable<Proveedor[]> {
    return this.http.get<Proveedor[]>(this.urlProveedores);
  }

  crearProveedor(proveedor: Partial<Proveedor>): Observable<Proveedor> {
    return this.http.post<Proveedor>(this.urlProveedores, proveedor);
  }

  actualizarProveedorById(id: number, proveedor: Partial<Proveedor>): Observable<Proveedor> {
    return this.http.put<Proveedor>(`${this.urlProveedores}/${id}`, proveedor);
  }

  eliminarProveedorById(id: number): Observable<string> {
    return this.http.delete<string>(`${this.urlProveedores}/${id}`);
  }

  // ---- Ventas ----

  getVentas(): Observable<unknown[]> {
    return this.http.get<unknown[]>(this.urlVentas);
  }

  actualizarEstadoVentaById(id: number, estado: string): Observable<unknown> {
    return this.http.put<unknown>(`${this.urlVentas}/${id}/estado`, { estado });
  }
}
