import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReporteService } from '../../../service/reporte.service';
import { PageResponse } from '../../../models/admin';
import { Tabla, ColumnaTabla } from '../../../shared/tabla/tabla';
import { Boton } from '../../../shared/boton/boton';
import { Spinner } from '../../../shared/spinner/spinner';
import { EstadoVacio } from '../../../shared/estado-vacio/estado-vacio';

@Component({
  selector: 'app-kardex',
  standalone: true,
  imports: [CommonModule, FormsModule, Tabla, Boton, Spinner, EstadoVacio],
  templateUrl: './kardex.html',
  styleUrl: './kardex.css',
})
export class KardexComponent implements OnInit {
  pagina: PageResponse<any> | null = null;
  filtros = {
    fechaDesde: '',
    fechaHasta: '',
    productoId: null as number | null,
    tipo: '',
    usuarioId: null as number | null,
    pagina: 0,
    tamano: 20,
  };

  cargando = false;
  error = false;

  columnas: ColumnaTabla[] = [
    { clave: 'fechaHora', titulo: 'Fecha/Hora', valor: (f) => (f.fechaHora ? new Date(f.fechaHora).toLocaleString() : '—') },
    { clave: 'insumo', titulo: 'Insumo' },
    { clave: 'tipo', titulo: 'Tipo' },
    { clave: 'cantidad', titulo: 'Cantidad', alineacion: 'derecha' },
    { clave: 'stockAnterior', titulo: 'Stock anterior', alineacion: 'derecha' },
    { clave: 'stockNuevo', titulo: 'Stock nuevo', alineacion: 'derecha' },
    { clave: 'motivo', titulo: 'Motivo' },
    { clave: 'usuario', titulo: 'Usuario', valor: (f) => f.usuario || 'Sistema' },
  ];

  constructor(private reporteService: ReporteService) {}

  get kardexData(): any[] {
    return this.pagina?.contenido ?? [];
  }

  get totalPaginas(): number {
    return this.pagina?.totalPaginas ?? 0;
  }

  ngOnInit(): void {
    this.buscarKardex();
  }

  buscarKardex(): void {
    this.cargando = true;
    this.error = false;

    this.reporteService.obtenerKardex(this.filtros).subscribe({
      next: (p) => {
        this.pagina = p;
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error al cargar el Kardex:', err);
        this.error = true;
        this.cargando = false;
      },
    });
  }

  descargarCSV(): void {
    this.reporteService.descargarKardexCsv(this.filtros);
  }

  limpiarFiltros(): void {
    this.filtros = { fechaDesde: '', fechaHasta: '', productoId: null, tipo: '', usuarioId: null, pagina: 0, tamano: 20 };
    this.buscarKardex();
  }

  irPagina(p: number): void {
    if (p < 0 || p >= this.totalPaginas) return;
    this.filtros.pagina = p;
    this.buscarKardex();
  }
}
