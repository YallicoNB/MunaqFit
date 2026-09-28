import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../core/api';
import { Bebida } from '../models/producto';
import {
  ClienteFidelidad,
  DetalleVentaRespuesta,
  PagoRequest,
  PagoRespuesta,
  Venta,
  VentaRequest,
} from '../models/venta';

@Injectable({
  providedIn: 'root',
})
export class VentaService {
  private apiUrl = `${API_URL}/empleado/ventas`;
  private urlOrdenes = `${API_URL}/empleado/ordenes`;
  private urlMenu = `${API_URL}/empleado/menu`;
  private urlFidelidad = `${API_URL}/empleado/fidelidad`;

  constructor(private http: HttpClient) {}

  // ---- Ventas ----

  /** Registra un pedido. El backend descuenta el stock de los insumos. */
  registrar(pedido: VentaRequest): Observable<Venta> {
    return this.http.post<Venta>(`${this.apiUrl}/registrar`, pedido);
  }

  /** Cobra la venta: guarda el pago y la pasa a PAGADO. */
  registrarPago(pago: PagoRequest): Observable<PagoRespuesta> {
    return this.http.post<PagoRespuesta>(`${this.apiUrl}/pago`, pago);
  }

  /** Solo calcula el vuelto, sin registrar nada. */
  calcularVuelto(total: number, recibido: number): Observable<{ vuelto: number }> {
    return this.http.post<{ vuelto: number }>(`${this.apiUrl}/vuelto`, { total, recibido });
  }

  // ---- Ordenes ----

  historial(): Observable<Venta[]> {
    return this.http.get<Venta[]>(`${this.urlOrdenes}/historial`);
  }

  detalleOrden(ventaId: number): Observable<DetalleVentaRespuesta[]> {
    return this.http.get<DetalleVentaRespuesta[]>(`${this.urlOrdenes}/${ventaId}/detalle`);
  }

  // ---- Menu ----

  menu(): Observable<Bebida[]> {
    return this.http.get<Bebida[]>(`${this.urlMenu}/bebidas`);
  }

  // ---- Fidelidad ----

  registrarCliente(cliente: Partial<ClienteFidelidad>): Observable<ClienteFidelidad> {
    return this.http.post<ClienteFidelidad>(`${this.urlFidelidad}/registrar`, cliente);
  }

  /** Suma una visita. Si completa el umbral la respuesta trae el premio. */
  registrarVisita(clienteId: number): Observable<string> {
    return this.http.post<string>(`${this.urlFidelidad}/${clienteId}/visita`, {});
  }
}
