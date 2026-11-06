import { Component, OnInit } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
// NUEVO: Importar ng2-charts
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartType } from 'chart.js';

import { ReporteService } from '../../../service/reporte.service';
import { RankingBebida, ReporteVentas } from '../../../models/admin';

@Component({
  selector: 'app-reportes',
  standalone: true, // Asegúrate de que esté como standalone
  imports: [CommonModule, DecimalPipe, FormsModule, BaseChartDirective], // Agregado BaseChartDirective
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

  // --- REQ-029: Configuración de Gráficos (Chart.js) ---

  // Gráfico 1: Ranking (Barras)
  public barChartType: ChartType = 'bar';
  public barChartData: ChartConfiguration['data'] = {
    labels: [],
    datasets: [{ data: [], label: 'Cantidad Vendida', backgroundColor: '#4caf50' }]
  };
  public barChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } }
  };

  // Gráfico 2: Métodos de Pago (Pie)
  public pieChartType: ChartType = 'pie';
  public pieChartData: ChartConfiguration['data'] = {
    labels: [],
    datasets: [{ data: [] }]
  };
  public pieChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
  };

  constructor(private reporteService: ReporteService) {}

  ngOnInit(): void {
    // Por defecto, el último mes
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
      next: (r) => {
        this.reporte = r;
        this.actualizarGraficoPagos(); // Actualizamos el gráfico
      },
      error: () => (this.error = 'No se pudo cargar el reporte de ventas'),
    });

    this.reporteService.ranking(inicio, fin).subscribe({
      next: (r) => {
        this.ranking = r;
        this.actualizarGraficoRanking(); // Actualizamos el gráfico
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

  // --- REQ-029: Actualización dinámica de los datos de los gráficos ---
  private actualizarGraficoRanking(): void {
    // Tomamos el top 5 para que el gráfico no se sature
    const top5 = this.ranking.slice(0, 5);
    this.barChartData = {
      labels: top5.map(item => item.nombreBebida),
      datasets: [{
        data: top5.map(item => item.cantidadVendida),
        label: 'Cantidad Vendida',
        backgroundColor: '#4caf50'
      }]
    };
  }

  private actualizarGraficoPagos(): void {
    if (!this.reporte || !this.reporte.ingresosPorMetodoPago) return;

    this.pieChartData = {
      labels: this.clavesPago,
      datasets: [{
        data: this.clavesPago.map(k => this.reporte!.ingresosPorMetodoPago[k]),
        backgroundColor: ['#ff6384', '#36a2eb', '#cc65fe', '#ffce56', '#4bc0c0']
      }]
    };
  }

  // --- REQ-032: Simulación de Exportación ---
  exportarPDF(): void {
    alert('Función de exportación a PDF en construcción. (Se conectará con la librería jspdf)');
  }
}
