import { Component, OnInit } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReporteService } from '../../../service/reporte.service';
import { RankingBebida, ReporteVentas } from '../../../models/admin';

@Component({
  selector: 'app-reportes',
  imports: [CommonModule, DecimalPipe, FormsModule],
  templateUrl: './reportes.html',
  styleUrl: './reportes.css',
})
export class Reportes implements OnInit {
  reporte: ReporteVentas | null = null;
  ranking: RankingBebida[] = [];
  valorInventario = 0;
  cargando = true;
  error = '';

  // Filtro de fechas
  desde = '';
  hasta = '';
  filtrado = false;

  constructor(private reporteService: ReporteService) {}

  ngOnInit(): void {
    // Por defecto, el ultimo mes
    const fin = new Date();
    const inicio = new Date();
    inicio.setMonth(inicio.getMonth() - 1);
    this.desde = inicio.toISOString().slice(0, 10);
    this.hasta = fin.toISOString().slice(0, 10);
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.error = '';
    this.filtrado = true;

    // El backend espera fecha y hora completas
    const inicio = `${this.desde}T00:00:00`;
    const fin = `${this.hasta}T23:59:59`;

    this.reporteService.ventas(inicio, fin).subscribe({
      next: (r) => (this.reporte = r),
      error: () => (this.error = 'No se pudo cargar el reporte de ventas'),
    });

    this.reporteService.ranking(inicio, fin).subscribe({
      next: (r) => (this.ranking = r),
      error: () => (this.error = 'No se pudo cargar el ranking'),
    });

    this.reporteService.valorizacion().subscribe({
      next: (v) => (this.valorInventario = v),
      error: () => (this.error = 'No se pudo cargar la valorizacion'),
    });

    setTimeout(() => (this.cargando = false), 500);
  }

  get clavesPago(): string[] {
    return this.reporte ? Object.keys(this.reporte.ingresosPorMetodoPago ?? {}) : [];
  }
}
