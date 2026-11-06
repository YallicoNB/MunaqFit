import { Component, OnInit } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../service/admin.service';
import { ProductoInventario, PageResponse } from '../../../models/admin';
import { Tabla, ColumnaTabla } from '../../../shared/tabla/tabla';
import { Boton } from '../../../shared/boton/boton';
import { Modal } from '../../../shared/modal/modal';
import { Badge } from '../../../shared/badge/badge';
import { EstadoVacio } from '../../../shared/estado-vacio/estado-vacio';
import { Spinner } from '../../../shared/spinner/spinner';

@Component({
  selector: 'app-inventario',
  imports: [CommonModule, DecimalPipe, FormsModule, Tabla, Boton, Modal, Badge, EstadoVacio, Spinner],
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
  cantidad = 0;
  motivo = '';
  guardando = false;
  productoSeleccionado: ProductoInventario | null = null;

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

  constructor(private adminService: AdminService) {}

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
    this.cantidad = 0;
    this.motivo = `Reabastecimiento de ${producto.nombre}`;
    this.mostrarFormulario = true;
    this.mensaje = '';
    this.error = '';
  }

  reabastecer(): void {
    if (this.cantidad <= 0) {
      this.error = 'La cantidad debe ser mayor a cero';
      return;
    }

    this.guardando = true;
    this.error = '';
    this.mensaje = '';

    this.adminService
      .reabastecer({ productoId: this.productoId, cantidad: this.cantidad, motivo: this.motivo })
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
