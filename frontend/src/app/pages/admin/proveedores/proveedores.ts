import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AdminService } from '../../../service/admin.service';
import { Proveedor } from '../../../models/admin';
import { ToastService } from '../../../shared/toast/toast.service';
import { CampoInput } from '../../../shared/input/input';

@Component({
  selector: 'app-proveedores',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, CampoInput],
  templateUrl: './proveedores.html',
  styleUrl: './proveedores.css'
})
export class Proveedores implements OnInit {
  proveedores: Proveedor[] = [];
  cargando = true;

  // Estado del formulario reactivo.
  mostrarFormulario = false;
  guardando = false;
  modoEdicion = false;
  idEditando: number | null = null;
  private proveedorOriginal: Proveedor | null = null;
  formulario: FormGroup;

  constructor(
    private fb: FormBuilder,
    private adminService: AdminService,
    private toast: ToastService
  ) {
    this.formulario = this.fb.group({
      ruc: ['', [Validators.required, Validators.pattern(/^\d{11}$/)]],
      nombre: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      contactoNombre: ['', [Validators.maxLength(100)]],
      telefono: ['', [Validators.pattern(/^$|^\d{9}$/)]],
    });
  }

  ngOnInit(): void {
    this.cargarProveedores();
  }

  get ruc() {
    return this.formulario.get('ruc')!;
  }

  get nombre() {
    return this.formulario.get('nombre')!;
  }

  get telefono() {
    return this.formulario.get('telefono')!;
  }

  /** Mensaje específico para el RUC; el resto los deriva app-input. */
  get errorRuc(): string {
    if (!this.ruc.touched || this.ruc.valid) return '';
    if (this.ruc.hasError('required')) return 'El RUC es obligatorio';
    return 'El RUC debe tener 11 dígitos numéricos';
  }

  get errorTelefono(): string {
    if (!this.telefono.touched || this.telefono.valid) return '';
    return 'El teléfono debe tener 9 dígitos';
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
    this.formulario.reset({ ruc: '', nombre: '', contactoNombre: '', telefono: '' });
    this.idEditando = null;
    this.proveedorOriginal = null;
    this.modoEdicion = false;
    this.mostrarFormulario = true;
  }

  abrirFormularioEdicion(proveedor: Proveedor): void {
    // patchValue no toca las validaciones ni muta la fila de la tabla.
    this.formulario.reset({
      ruc: proveedor.ruc ?? '',
      nombre: proveedor.nombre ?? '',
      contactoNombre: proveedor.contactoNombre ?? '',
      telefono: proveedor.telefono ?? '',
    });
    this.idEditando = proveedor.id;
    this.proveedorOriginal = proveedor;
    this.modoEdicion = true;
    this.mostrarFormulario = true;
  }

  cerrarFormulario(): void {
    this.mostrarFormulario = false;
    this.formulario.reset({ ruc: '', nombre: '', contactoNombre: '', telefono: '' });
    this.idEditando = null;
    this.proveedorOriginal = null;
    this.modoEdicion = false;
  }

  guardar(): void {
    if (this.guardando) return;

    // Normaliza antes de validar para que espacios accidentales no bloqueen.
    this.formulario.patchValue(
      {
        ruc: String(this.formulario.value.ruc ?? '').trim(),
        nombre: String(this.formulario.value.nombre ?? '').trim(),
        contactoNombre: String(this.formulario.value.contactoNombre ?? '').trim(),
        telefono: String(this.formulario.value.telefono ?? '').trim(),
      },
      { emitEvent: false }
    );

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      this.toast.error('Revisa los campos marcados en rojo', 'Formulario incompleto');
      return;
    }

    const datos = this.datosFormulario();
    this.guardando = true;

    if (this.modoEdicion && this.idEditando !== null) {
      this.adminService.actualizarProveedor(this.idEditando, datos).subscribe({
        next: () => {
          this.toast.success('Proveedor actualizado correctamente', 'Éxito');
          this.finalizarGuardado();
        },
        error: (err) => this.manejarError(err)
      });
    } else {
      this.adminService.crearProveedor(datos).subscribe({
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

  private datosFormulario(): Partial<Proveedor> {
    const valor = this.formulario.getRawValue();
    const datos: Partial<Proveedor> = {
      ruc: String(valor.ruc).trim(),
      nombre: String(valor.nombre).trim(),
      contactoNombre: String(valor.contactoNombre ?? '').trim() || null,
      telefono: String(valor.telefono ?? '').trim() || null,
    };

    // El backend reemplaza estos campos con lo que recibe: conservamos los
    // que no se editan en este formulario para no borrarlos.
    if (this.modoEdicion && this.proveedorOriginal) {
      datos.estado = this.proveedorOriginal.estado;
      datos.direccion = this.proveedorOriginal.direccion;
      datos.tipoContrato = this.proveedorOriginal.tipoContrato;
    }

    return datos;
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
