import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../core/api';
import { DashboardMetrics, ReporteVentas, RankingBebida } from '../models/admin';

@Injectable({
  providedIn: 'root',
})
export class ReporteService {
  private urlDashboard = `${API_URL}/admin/dashboard`;
  private urlReportes = `${API_URL}/admin/reportes`;

  constructor(private http: HttpClient) {}

  metricas(): Observable<DashboardMetrics> {
    return this.http.get<DashboardMetrics>(`${this.urlDashboard}/metricas`);
  }

  ventas(inicio: string, fin: string): Observable<ReporteVentas> {
    const params = new HttpParams().set('inicio', inicio).set('fin', fin);
    return this.http.get<ReporteVentas>(`${this.urlReportes}/ventas`, { params });
  }

  ranking(inicio: string, fin: string): Observable<RankingBebida[]> {
    const params = new HttpParams().set('inicio', inicio).set('fin', fin);
    return this.http.get<RankingBebida[]>(`${this.urlReportes}/ranking`, { params });
  }

  valorizacion(): Observable<number> {
    return this.http.get<number>(`${this.urlReportes}/valorizacion`);
  }

  /** Rango por defecto: el ultimo mes. */
  private ultimoMes(): { inicio: string; fin: string } {
    const fin = new Date();
    const inicio = new Date();
    inicio.setMonth(inicio.getMonth() - 1);
    return { inicio: inicio.toISOString(), fin: fin.toISOString() };
  }

  ventasUltimoMes(): Observable<ReporteVentas> {
    const { inicio, fin } = this.ultimoMes();
    return this.ventas(inicio, fin);
  }

  rankingUltimoMes(): Observable<RankingBebida[]> {
    const { inicio, fin } = this.ultimoMes();
    return this.ranking(inicio, fin);
  }
 obtenerKardex(filtros: any): Observable<any[]> {
    let params = new HttpParams();
    if (filtros.fechaDesde) params = params.set('fechaDesde', filtros.fechaDesde);
    if (filtros.fechaHasta) params = params.set('fechaHasta', filtros.fechaHasta);
    if (filtros.productoId) params = params.set('productoId', filtros.productoId);
    if (filtros.tipo) params = params.set('tipo', filtros.tipo);
    if (filtros.usuarioId) params = params.set('usuarioId', filtros.usuarioId);

    return this.http.get<any[]>(`${this.urlReportes}/kardex`, { params });
  }

  descargarKardexCsv(filtros: any): void {
    let params = new HttpParams();
    if (filtros.fechaDesde) params = params.set('fechaDesde', filtros.fechaDesde);
    if (filtros.fechaHasta) params = params.set('fechaHasta', filtros.fechaHasta);
    if (filtros.productoId) params = params.set('productoId', filtros.productoId);
    if (filtros.tipo) params = params.set('tipo', filtros.tipo);
    if (filtros.usuarioId) params = params.set('usuarioId', filtros.usuarioId);

    this.http.get(`${this.urlReportes}/kardex/csv`, { params, responseType: 'blob' }).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'kardex_reporte.csv';
      a.click();
      window.URL.revokeObjectURL(url);
    });
  }
}
