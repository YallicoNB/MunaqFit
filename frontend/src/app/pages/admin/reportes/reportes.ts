import { Component, OnInit } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartType } from 'chart.js';

import { ReporteService } from '../../../service/reporte.service';
import { RankingBebida, ReporteVentas } from '../../../models/admin';

@Component({
  selector: 'app-reportes',
  standalone: true,
  imports: [CommonModule, DecimalPipe, ReactiveFormsModule, BaseChartDirective],
  templateUrl: './reportes.html',
  styleUrl: './reportes.css',
})
export class Reportes implements OnInit {
  reporte: ReporteVentas | null = null;
  ranking: RankingBebida[] = [];
  valorInventario = 0;
  cargando = true;
  error = '';

  // Filtro de fechas (formulario reactivo, tarea 4.5)
  formFiltros: FormGroup;
  filtrado = false;
  periodo: 'dia' | 'semana' | 'mes' | 'personalizado' = 'mes';

  // --- REQ-029: Configuración de Gráficos (Chart.js) ---

  // Gráfico 1: Ranking (Barras)
  public barChartType: ChartType = 'bar';
  public barChartData: ChartConfiguration['data'] = {
    labels: [],
    datasets: [{ data: [], label: 'Cantidad Vendida', backgroundColor: '#4caf50' }],
  };
  public barChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
  };

  // Gráfico 2: Métodos de Pago (Pie)
  public pieChartType: ChartType = 'pie';
  public pieChartData: ChartConfiguration['data'] = {
    labels: [],
    datasets: [{ data: [] }],
  };
  public pieChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
  };

  constructor(private fb: FormBuilder, private reporteService: ReporteService) {
    this.formFiltros = this.fb.group({
      desde: ['', [Validators.required]],
      hasta: ['', [Validators.required]],
    });
  }

  get desde(): string {
    return this.formFiltros.get('desde')?.value ?? '';
  }

  get hasta(): string {
    return this.formFiltros.get('hasta')?.value ?? '';
  }

  ngOnInit(): void {
    const fin = new Date();
    const inicio = new Date();
    inicio.setMonth(inicio.getMonth() - 1);
    this.formFiltros.setValue({
      desde: inicio.toISOString().slice(0, 10),
      hasta: fin.toISOString().slice(0, 10),
    });
    this.cargar();
  }

  cargar(): void {
    if (this.formFiltros.invalid) {
      this.formFiltros.markAllAsTouched();
      return;
    }

    this.cargando = true;
    this.error = '';
    this.filtrado = true;

    const inicio = `${this.desde}T00:00:00`;
    const fin = `${this.hasta}T23:59:59`;

    this.reporteService.ventas(inicio, fin).subscribe({
      next: (r) => {
        this.reporte = r;
        this.actualizarGraficoPagos();
      },
      error: () => (this.error = 'No se pudo cargar el reporte de ventas'),
    });

    this.reporteService.ranking(inicio, fin).subscribe({
      next: (r) => {
        this.ranking = r;
        this.actualizarGraficoRanking();
      },
      error: () => (this.error = 'No se pudo cargar el ranking'),
    });

    this.reporteService.valorizacion().subscribe({
      next: (v) => (this.valorInventario = v),
      error: () => (this.error = 'No se pudo cargar la valorización'),
    });

    setTimeout(() => (this.cargando = false), 500);
  }

  get clavesPago(): string[] {
    return this.reporte ? Object.keys(this.reporte.ingresosPorMetodoPago ?? {}) : [];
  }

  private actualizarGraficoRanking(): void {
    const top5 = this.ranking.slice(0, 5);
    this.barChartData = {
      labels: top5.map((item) => item.nombreBebida),
      datasets: [
        {
          data: top5.map((item) => item.cantidadVendida),
          label: 'Cantidad Vendida',
          backgroundColor: '#4caf50',
        },
      ],
    };
  }

  private actualizarGraficoPagos(): void {
    if (!this.reporte || !this.reporte.ingresosPorMetodoPago) return;

    this.pieChartData = {
      labels: this.clavesPago,
      datasets: [
        {
          data: this.clavesPago.map((k) => this.reporte!.ingresosPorMetodoPago[k]),
          backgroundColor: ['#ff6384', '#36a2eb', '#cc65fe', '#ffce56', '#4bc0c0'],
        },
      ],
    };
  }

  // --- REQ-032: exportación real a CSV ---
  exportarCSV(): void {
    if (!this.reporte) return;

    const filas: string[][] = [];
    filas.push(['Reporte', 'Valor']);
    filas.push(['Total ingresos', String(this.reporte.totalIngresos)]);
    filas.push(['Total transacciones', String(this.reporte.totalTransacciones)]);
    filas.push(['Promedio diario', String(this.reporte.promedioDiario)]);
    filas.push(['Producto más vendido', this.reporte.productoMasVendido ?? '—']);
    filas.push([]);
    filas.push(['Método de pago', 'Ingresos']);
    for (const clave of this.clavesPago) {
      filas.push([clave, String(this.reporte.ingresosPorMetodoPago[clave])]);
    }
    filas.push([]);
    filas.push(['Puesto', 'Bebida', 'Cantidad', 'Recaudado']);
    for (const item of this.ranking) {
      filas.push([
        String(item.posicion),
        item.nombreBebida,
        String(item.cantidadVendida),
        String(item.totalRecaudado),
      ]);
    }

    const csv = filas
      .map((fila) => fila.map((celda) => `"${(celda ?? '').replace(/"/g, '""')}"`).join(','))
      .join('\r\n');

    const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `reporte_${this.desde}_${this.hasta}.csv`;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  /** Atajos de período: día, semana o mes (REQ-029). */
  aplicarPeriodo(p: 'dia' | 'semana' | 'mes'): void {
    this.periodo = p;
    const fin = new Date();
    const inicio = new Date();

    if (p === 'dia') {
      inicio.setDate(fin.getDate());
    } else if (p === 'semana') {
      inicio.setDate(fin.getDate() - 6);
    } else {
      inicio.setMonth(fin.getMonth() - 1);
    }

    this.formFiltros.setValue({
      desde: inicio.toISOString().slice(0, 10),
      hasta: fin.toISOString().slice(0, 10),
    });
    this.cargar();
  }

  /** Compatibilidad con el botón existente: exporta CSV. */
  exportarPDF(): void {
    this.exportarCSV();
  }
}
