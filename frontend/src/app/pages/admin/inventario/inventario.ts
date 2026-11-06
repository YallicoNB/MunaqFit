import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AdminService } from '../../../service/admin.service';
import { ProductoInventario, PageResponse } from '../../../models/admin';
import { Tabla, ColumnaTabla } from '../../../shared/tabla/tabla';
import { Boton } from '../../../shared/boton/boton';
import { Modal } from '../../../shared/modal/modal';
import { CampoInput } from '../../../shared/input/input';

@Component({
  selector: 'app-inventario',
  imports: [CommonModule, FormsModule, ReactiveFormsModule, Tabla, Boton, Modal, CampoInput],
  templateUrl: './inventario.html',
  styleUrl: './inventario.css',
})
export class Inventario implements OnInit {
  pagina: PageResponse<ProductoInventario> | null = null;
  criticos: ProductoInventario[] = [];
  bajos: ProductoInventario[] = [];
  busqueda = '';
  paginaActual = 0;
  tamano = 20;
  cargando = true;
  error = '';
  mensaje = '';

  // Formulario de reabastecimiento
  mostrarFormulario = false;
  productoId = 0;
  productoSeleccionado: ProductoInventario | null = null;
  guardando = false;
  formReabastecer: FormGroup;

  columnas: ColumnaTabla[] = [
    { clave: 'nombre', titulo: 'Insumo' },
    { clave: 'categoria', titulo: 'Categoría' },
    { clave: 'stockActual', titulo: 'Stock', alineacion: 'centro' },
    { clave: 'stockMinimo', titulo: 'Mín.', alineacion: 'centro' },
    { clave: 'stockCritico', titulo: 'Crítico', alineacion: 'centro' },
    { clave: 'unidadMedida', titulo: 'Unidad', alineacion: 'centro' },
    { clave: 'costoUnitario', titulo: 'Costo', alineacion: 'derecha' },
    { clave: 'ultimaReposicion', titulo: 'Últ. reposición' },
  ];

  constructor(private fb: FormBuilder, private adminService: AdminService) {
    this.formReabastecer = this.fb.group({
      cantidad: [0, [Validators.required, Validators.min(0.01)]],
      motivo: ['', [Validators.required, Validators.minLength(3)]],
    });
  }

  get productos(): ProductoInventario[] {
    return this.pagina?.contenido ?? [];
  }

  get totalPaginas(): number {
    return this.pagina?.totalPaginas ?? 0;
  }

  get productosFiltrados(): ProductoInventario[] {
    return this.productos;
  }

  abreviarUnidad(unidad: string): string {
    return unidad === 'UNIDAD' ? 'U' : unidad;
  }

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.error = '';

    this.adminService.inventario(this.paginaActual, this.tamano, this.busqueda).subscribe({
      next: (p) => {
        this.pagina = p;
        this.cargando = false;
      },
      error: () => {
        this.error = 'No se pudo cargar el inventario';
        this.cargando = false;
      },
    });

    this.adminService.stockCritico().subscribe((p) => (this.criticos = p));
    this.adminService.stockBajo().subscribe((p) => (this.bajos = p));
  }

  buscar(): void {
    this.paginaActual = 0;
    this.cargar();
  }

  irPagina(p: number): void {
    if (p < 0 || p >= this.totalPaginas) return;
    this.paginaActual = p;
    this.cargar();
  }

  /** Nivel de stock de un producto: critico, bajo o normal. */
  nivel(p: ProductoInventario): 'critico' | 'bajo' | 'normal' {
    if (p.estadoStock === 'CRITICO' || p.stockActual <= p.stockCritico) return 'critico';
    if (p.estadoStock === 'BAJO' || p.stockActual <= p.stockMinimo) return 'bajo';
    return 'normal';
  }

  /** Nombres de los productos en stock critico, para la alerta. */
  get nombresCriticos(): string {
    return this.criticos.map((p) => p.nombre).join(', ');
  }

  abrirFormulario(producto: ProductoInventario): void {
    this.productoSeleccionado = producto;
    this.productoId = producto.id;
    this.formReabastecer.reset({
      cantidad: 0,
      motivo: `Reabastecimiento de ${producto.nombre}`,
    });
    this.mostrarFormulario = true;
    this.mensaje = '';
    this.error = '';
  }

  reabastecer(): void {
    if (this.formReabastecer.invalid) {
      this.formReabastecer.markAllAsTouched();
      return;
    }

    this.guardando = true;
    this.error = '';
    this.mensaje = '';

    const { cantidad, motivo } = this.formReabastecer.value;

    this.adminService
      .reabastecer({ productoId: this.productoId, cantidad, motivo })
      .subscribe({
        next: (respuesta) => {
          this.mensaje = respuesta || 'Stock actualizado';
          this.guardando = false;
          this.mostrarFormulario = false;
          this.cargar();
        },
        error: (e) => {
          this.error = e?.error?.message ?? e?.error ?? 'No se pudo reabastecer';
          this.guardando = false;
        },
      });
  }
}
