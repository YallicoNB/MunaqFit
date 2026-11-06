import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../core/api';
import { ProductoInventario, ReabastecerRequest, Usuario, Proveedor, CrearUsuarioRequest, Rol, PageResponse } from '../models/admin';


@Injectable({
  providedIn: 'root',
})
export class AdminService {
  private urlInventario = `${API_URL}/admin/inventario`;
  private urlUsuarios = `${API_URL}/admin/usuarios`;
  private urlProveedores = `${API_URL}/admin/proveedores`;
  private urlVentas = `${API_URL}/admin/ventas`;

  constructor(private http: HttpClient) { }

  // ---- Inventario ----

  inventario(
    pagina = 0,
    tamano = 20,
    busqueda?: string,
    categoriaId?: number,
    soloBajo = false
  ): Observable<PageResponse<ProductoInventario>> {
    let params = new HttpParams()
      .set('pagina', pagina)
      .set('tamano', tamano);
    if (busqueda) params = params.set('busqueda', busqueda);
    if (categoriaId) params = params.set('categoriaId', categoriaId);
    if (soloBajo) params = params.set('soloBajo', 'true');
    return this.http.get<PageResponse<ProductoInventario>>(this.urlInventario, { params });
  }

  stockCritico(): Observable<ProductoInventario[]> {
    return this.http.get<ProductoInventario[]>(`${this.urlInventario}/critico`);
  }

  stockBajo(): Observable<ProductoInventario[]> {
    return this.http.get<ProductoInventario[]>(`${this.urlInventario}/bajo`);
  }

  reabastecer(datos: ReabastecerRequest): Observable<string> {
    return this.http.post<string>(`${this.urlInventario}/reabastecer`, datos);
  }

  // ---- Usuarios ----

  listarUsuarios(
    pagina = 0,
    tamano = 20,
    busqueda?: string,
    estado?: string
  ): Observable<PageResponse<Usuario>> {
    let params = new HttpParams()
      .set('pagina', pagina)
      .set('tamano', tamano);
    if (busqueda) params = params.set('busqueda', busqueda);
    if (estado) params = params.set('estado', estado);
    return this.http.get<PageResponse<Usuario>>(this.urlUsuarios, { params });
  }

  /** Crea un empleado nuevo. El backend siempre le asigna el rol EMPLEADO. */
  crearUsuario(usuario: CrearUsuarioRequest): Observable<Usuario> {
    return this.http.post<Usuario>(this.urlUsuarios, usuario);
  }

  /** Inactiva al empleado (soft delete). */
  eliminarUsuario(id: number): Observable<string> {
    return this.http.delete<string>(`${this.urlUsuarios}/${id}`);
  }

  /** Cambia el rol de un usuario (solo ADMIN). */
  cambiarRol(id: number, rol: Rol): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.urlUsuarios}/${id}/rol`, { rol });
  }

  // ---- Proveedores ----

  listarProveedores(): Observable<Proveedor[]> {
    return this.http.get<Proveedor[]>(this.urlProveedores);
  }

  crearProveedor(proveedor: Partial<Proveedor>): Observable<Proveedor> {
    return this.http.post<Proveedor>(this.urlProveedores, proveedor);
  }

  actualizarProveedor(id: number, proveedor: Partial<Proveedor>): Observable<Proveedor> {
    return this.http.put<Proveedor>(`${this.urlProveedores}/${id}`, proveedor);
  }

  eliminarProveedor(id: number): Observable<string> {
    return this.http.delete<string>(`${this.urlProveedores}/${id}`);
  }

  // ---- Ventas ----

  listarVentas(filtros?: any): Observable<PageResponse<any>> {
    let params = new HttpParams()
      .set('pagina', filtros?.pagina ?? 0)
      .set('tamano', filtros?.tamano ?? 20);

    if (filtros?.fecha) params = params.set('fecha', filtros.fecha);
    if (filtros?.empleadoId) params = params.set('empleadoId', filtros.empleadoId);
    if (filtros?.estado) params = params.set('estado', filtros.estado);

    return this.http.get<PageResponse<any>>(this.urlVentas, { params });
  }

  cambiarEstadoVenta(id: number, estado: string): Observable<unknown> {
    return this.http.put<unknown>(`${this.urlVentas}/${id}/estado`, null, {
      params: new HttpParams().set('nuevoEstado', estado),
    });
  }
}
