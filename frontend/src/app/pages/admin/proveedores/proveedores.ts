import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../service/admin.service';
import { Proveedor } from '../../../models/admin';
import { ToastService } from '../../../shared/toast/toast.service';

@Component({
  selector: 'app-proveedores',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './proveedores.html'
})
export class Proveedores implements OnInit {
  proveedores: Proveedor[] = [];
  cargando: boolean = true;

  // Variables para el formulario
  mostrarFormulario: boolean = false;
  guardando: boolean = false;
  modoEdicion: boolean = false;

  // Objeto temporal para crear/editar
  proveedorActual: Partial<Proveedor> = {};

  constructor(
    private adminService: AdminService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.cargarProveedores();
  }

  cargarProveedores(): void {
    this.cargando = true;
    this.adminService.listarProveedores().subscribe({
      next: (data) => {
        this.proveedores = data;
        this.cargando = false;
      },
      error: () => {
        this.toast.error('Error al cargar proveedores', 'Proveedores');
        this.cargando = false;
      }
    });
  }

  abrirFormularioNuevo(): void {
    this.proveedorActual = { ruc: '', nombre: '', contactoNombre: '', telefono: '' };
    this.modoEdicion = false;
    this.mostrarFormulario = true;
  }

  abrirFormularioEdicion(proveedor: Proveedor): void {
    this.proveedorActual = { ...proveedor }; // Copia para no mutar la tabla directamente
    this.modoEdicion = true;
    this.mostrarFormulario = true;
  }

  cerrarFormulario(): void {
    this.mostrarFormulario = false;
    this.proveedorActual = {};
  }

  guardar(): void {
    if (!this.proveedorActual.ruc || !this.proveedorActual.nombre) {
      this.toast.error('RUC y Nombre son obligatorios', 'Formulario incompleto');
      return;
    }

    this.guardando = true;

    if (this.modoEdicion && this.proveedorActual.id) {
      // Actualizar
      this.adminService.actualizarProveedor(this.proveedorActual.id, this.proveedorActual).subscribe({
        next: () => {
          this.toast.success('Proveedor actualizado correctamente', 'Éxito');
          this.finalizarGuardado();
        },
        error: (err) => this.manejarError(err)
      });
    } else {
      // Crear
      this.adminService.crearProveedor(this.proveedorActual).subscribe({
        next: () => {
          this.toast.success('Proveedor creado correctamente', 'Éxito');
          this.finalizarGuardado();
        },
        error: (err) => this.manejarError(err)
      });
    }
  }

  eliminar(proveedor: Proveedor): void {
    if (!confirm(`¿Estás seguro de eliminar al proveedor ${proveedor.nombre}?`)) return;

    this.adminService.eliminarProveedor(proveedor.id!).subscribe({
      next: () => {
        this.toast.success('Proveedor eliminado', 'Éxito');
        this.cargarProveedores();
      },
      error: (err) => this.manejarError(err)
    });
  }

  private finalizarGuardado(): void {
    this.guardando = false;
    this.cerrarFormulario();
    this.cargarProveedores();
  }

  private manejarError(err: any): void {
    this.guardando = false;
    this.toast.error(err?.error?.message || 'Ocurrió un error inesperado', 'Error');
  }
}
