import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../service/admin.service';
import { ToastService } from '../../../shared/toast/toast.service';

@Component({
  selector: 'app-admin-ordenes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ordenes.html'
})
export class OrdenesAdmin implements OnInit {
  ventas: any[] = [];
  cargando: boolean = false;

  filtros = {
    fecha: '',
    empleadoId: '',
    estado: ''
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
      next: (data) => {
        this.ventas = data;
        this.cargando = false;
      },
      error: () => {
        this.toast.error('No se pudieron cargar las órdenes', 'Error');
        this.cargando = false;
      }
    });
  }

  aplicarFiltros(): void {
    this.cargarOrdenes();
  }

  limpiarFiltros(): void {
    this.filtros = { fecha: '', empleadoId: '', estado: '' };
    this.cargarOrdenes();
  }

  cambiarEstado(id: number, nuevoEstado: string): void {
    if (!confirm(`¿Estás seguro de cambiar el estado a ${nuevoEstado}?`)) return;

    this.adminService.cambiarEstadoVenta(id, nuevoEstado).subscribe({
      next: () => {
        this.toast.success('Estado actualizado correctamente', 'Órdenes');
        this.cargarOrdenes(); // Recargamos para ver el cambio reflejado
      },
      error: (err) => {
        this.toast.error(err?.error?.message || 'Error al actualizar el estado', 'Órdenes');
      }
    });
  }
}
