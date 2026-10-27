import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../service/admin.service';
import { ToastService } from '../../../shared/toast/toast.service';

@Component({
  selector: 'app-ordenes',
  imports: [CommonModule, FormsModule],
  templateUrl: './ordenes.html',
  styleUrl: './ordenes.css'
})
export class OrdenesAdmin implements OnInit {
  ventas: any[] = [];
  ventasFiltradas: any[] = [];

  filtroFecha: string = '';
  filtroEmpleado: string = '';
  filtroEstado: string = '';

  constructor(private adminService: AdminService, private toastService: ToastService) {}

  ngOnInit(): void {
    this.cargarVentas();
  }

  cargarVentas(): void {
    // Usando tu método listarVentas()
    this.adminService.listarVentas().subscribe({
      next: (data: any) => {
        this.ventas = data;
        this.ventasFiltradas = data;
      },
      error: () => this.toastService.error('Error al cargar las órdenes')
    });
  }

  aplicarFiltros(): void {
    this.ventasFiltradas = this.ventas.filter(venta => {
      const cumpleFecha = this.filtroFecha ? venta.fecha.includes(this.filtroFecha) : true;
      const cumpleEmpleado = this.filtroEmpleado ? venta.empleadoNombre.toLowerCase().includes(this.filtroEmpleado.toLowerCase()) : true;
      const cumpleEstado = this.filtroEstado ? venta.estado === this.filtroEstado : true;
      return cumpleFecha && cumpleEmpleado && cumpleEstado;
    });
  }

  limpiarFiltros(): void {
    this.filtroFecha = '';
    this.filtroEmpleado = '';
    this.filtroEstado = '';
    this.ventasFiltradas = this.ventas;
  }

  cambiarEstado(id: number, nuevoEstado: string): void {
    // Usando tu método cambiarEstadoVenta()
    this.adminService.cambiarEstadoVenta(id, nuevoEstado).subscribe({
      next: () => {
        this.toastService.success(`Estado actualizado a ${nuevoEstado}`);
        this.cargarVentas();
      },
      error: () => this.toastService.error('Error al actualizar estado')
    });
  }
}
