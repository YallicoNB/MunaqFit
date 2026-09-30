import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReporteService } from '../../../service/reporte.service';

@Component({
  selector: 'app-kardex',
  standalone: true,
  imports: [CommonModule, FormsModule], // Agregar aquí los importes de Shared cuando Dev 3 los termine
  templateUrl: './kardex.component.html'
})
export class KardexComponent implements OnInit {
  kardexData: any[] = [];
  filtros = {
    fechaDesde: '',
    fechaHasta: '',
    productoId: null,
    tipo: '',
    usuarioId: null
  };

  cargando: boolean = false;
  error: boolean = false;

  constructor(private reporteService: ReporteService) {}

  ngOnInit(): void {
    this.buscarKardex();
  }

  buscarKardex(): void {
    this.cargando = true;
    this.error = false;
    
    this.reporteService.obtenerKardex(this.filtros).subscribe({
      next: (data) => {
        this.kardexData = data;
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error al cargar el Kardex:', err);
        this.error = true;
        this.cargando = false;
      }
    });
  }

  descargarCSV(): void {
    this.reporteService.descargarKardexCsv(this.filtros);
  }

  limpiarFiltros(): void {
    this.filtros = { fechaDesde: '', fechaHasta: '', productoId: null, tipo: '', usuarioId: null };
    this.buscarKardex();
  }
}