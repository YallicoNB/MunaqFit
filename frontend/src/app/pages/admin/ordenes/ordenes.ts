import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../service/admin.service';
import { ToastService } from '../../../shared/toast/toast.service';

@Component({
  selector: 'app-admin-ordenes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ordenes.html',
  styleUrl: './ordenes.css'
})
export class OrdenesAdmin implements OnInit {
  ventas: any[] = [];
  cargando = false;
  paginaActual = 0;
  tamano = 20;
  totalPaginas = 0;

  filtros = {
    fecha: '',
    empleadoId: '',
    estado: '',
    pagina: 0,
    tamano: 20,
  };

  constructor(
    private adminService: AdminService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.cargarOrdenes();
  }

  cargarOrdenes(): void {
    this.cargando = true;
    this.adminService.listarVentas(this.filtros).subscribe({
      next: (p: any) => {
        this.ventas = p?.contenido ?? [];
        this.totalPaginas = p?.totalPaginas ?? 0;
        this.cargando = false;
      },
      error: () => {
        this.toast.error('No se pudieron cargar las órdenes', 'Error');
        this.cargando = false;
      }
    });
  }

  aplicarFiltros(): void {
    this.filtros.pagina = 0;
    this.paginaActual = 0;
    this.cargarOrdenes();
  }

  limpiarFiltros(): void {
    this.filtros = { fecha: '', empleadoId: '', estado: '', pagina: 0, tamano: 20 };
    this.paginaActual = 0;
    this.cargarOrdenes();
  }

  irPagina(p: number): void {
    if (p < 0 || p >= this.totalPaginas) return;
    this.filtros.pagina = p;
    this.paginaActual = p;
    this.cargarOrdenes();
  }

  cambiarEstado(id: number, nuevoEstado: string): void {
    if (!confirm(`¿Estás seguro de cambiar el estado a ${nuevoEstado}?`)) return;

    this.adminService.cambiarEstadoVenta(id, nuevoEstado).subscribe({
      next: () => {
        this.toast.success('Estado actualizado correctamente', 'Órdenes');
        this.cargarOrdenes();
      },
      error: (err) => {
        this.toast.error(err?.error?.message || 'Error al actualizar el estado', 'Órdenes');
      }
    });
  }
}
