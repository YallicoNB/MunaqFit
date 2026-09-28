import { Component, OnInit } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { ReporteService } from '../../service/reporte.service';
import { DashboardMetrics, RankingBebida, ReporteVentas } from '../../models/admin';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, DecimalPipe],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  metricas: DashboardMetrics | null = null;
  reporte: ReporteVentas | null = null;
  ranking: RankingBebida[] = [];
  valorInventario = 0;
  cargando = true;
  error = '';

  constructor(private reporteService: ReporteService) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.error = '';

    this.reporteService.metricas().subscribe({
      next: (m) => (this.metricas = m),
      error: () => (this.error = 'No se pudieron cargar las metricas'),
    });

    this.reporteService.ventasUltimoMes().subscribe({
      next: (r) => (this.reporte = r),
      error: () => (this.error = 'No se pudo cargar el reporte de ventas'),
    });

    this.reporteService.rankingUltimoMes().subscribe({
      next: (r) => (this.ranking = r),
      error: () => (this.error = 'No se pudo cargar el ranking'),
    });

    this.reporteService.valorizacion().subscribe({
      next: (v) => (this.valorInventario = v),
      error: () => (this.error = 'No se pudo cargar la valorizacion'),
    });

    setTimeout(() => (this.cargando = false), 600);
  }

  get clavesPago(): string[] {
    return this.reporte ? Object.keys(this.reporte.ingresosPorMetodoPago ?? {}) : [];
  }
}
