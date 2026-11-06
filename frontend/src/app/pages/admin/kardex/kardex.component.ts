import { Component, OnInit } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../service/admin.service';
import { ReporteService } from '../../../service/reporte.service';
import {
  FiltrosKardex,
  KardexFila,
  ProductoInventario,
  Usuario,
} from '../../../models/admin';

@Component({
  selector: 'app-kardex',
  standalone: true,
  imports: [CommonModule, DecimalPipe, FormsModule],
  templateUrl: './kardex.component.html',
})
export class KardexComponent implements OnInit {
  kardexData: KardexFila[] = [];
  productos: ProductoInventario[] = [];
  usuarios: Usuario[] = [];
  filtros: FiltrosKardex = {
    fechaDesde: '',
    fechaHasta: '',
    productoId: null,
    tipo: '',
    usuarioId: null,
  };

  cargando = false;
  descargando = false;
  error = '';
  errorOpciones = '';

  constructor(
    private reporteService: ReporteService,
    private adminService: AdminService,
  ) {}

  ngOnInit(): void {
    this.cargarOpciones();
    this.buscarKardex();
  }

  private cargarOpciones(): void {
    this.adminService.inventario().subscribe({
      next: (productos) => (this.productos = productos),
      error: () => (this.errorOpciones = 'No se pudieron cargar los insumos para el filtro.'),
    });
    this.adminService.listarUsuarios().subscribe({
      next: (usuarios) => (this.usuarios = usuarios),
      error: () => (this.errorOpciones = 'No se pudieron cargar los usuarios para el filtro.'),
    });
  }

  buscarKardex(): void {
    if (
      this.filtros.fechaDesde &&
      this.filtros.fechaHasta &&
      this.filtros.fechaDesde > this.filtros.fechaHasta
    ) {
      this.error = 'La fecha inicial no puede ser posterior a la fecha final.';
      return;
    }

    this.cargando = true;
    this.error = '';

    this.reporteService.obtenerKardex(this.filtros).subscribe({
      next: (filas) => {
        this.kardexData = filas;
        this.cargando = false;
      },
      error: () => {
        this.error = 'No se pudo cargar el reporte Kardex. Intenta nuevamente.';
        this.cargando = false;
      },
    });
  }

  descargarCSV(): void {
    if (
      this.filtros.fechaDesde &&
      this.filtros.fechaHasta &&
      this.filtros.fechaDesde > this.filtros.fechaHasta
    ) {
      this.error = 'La fecha inicial no puede ser posterior a la fecha final.';
      return;
    }

    this.descargando = true;
    this.error = '';
    this.reporteService.descargarKardexCsv(this.filtros).subscribe({
      next: (archivo) => {
        const url = window.URL.createObjectURL(archivo);
        const enlace = document.createElement('a');
        enlace.href = url;
        enlace.download = 'kardex_reporte.csv';
        document.body.appendChild(enlace);
        enlace.click();
        enlace.remove();
        window.setTimeout(() => window.URL.revokeObjectURL(url), 1000);
        this.descargando = false;
      },
      error: () => {
        this.error = 'No se pudo descargar el CSV. Intenta nuevamente.';
        this.descargando = false;
      },
    });
  }

  limpiarFiltros(): void {
    this.filtros = {
      fechaDesde: '',
      fechaHasta: '',
      productoId: null,
      tipo: '',
      usuarioId: null,
    };
    this.buscarKardex();
  }
}