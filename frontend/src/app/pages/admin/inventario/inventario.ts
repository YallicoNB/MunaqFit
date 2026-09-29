import { Component, OnInit } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../service/admin.service';
import { ProductoInventario } from '../../../models/admin';

@Component({
  selector: 'app-inventario',
  imports: [CommonModule, DecimalPipe, FormsModule],
  templateUrl: './inventario.html',
  styleUrl: './inventario.css',
})
export class Inventario implements OnInit {
  productos: ProductoInventario[] = [];
  criticos: ProductoInventario[] = [];
  bajos: ProductoInventario[] = [];
  busqueda = '';
  cargando = true;
  error = '';
  mensaje = '';

  // Formulario de reabastecimiento
  mostrarFormulario = false;
  productoId = 0;
  cantidad = 0;
  motivo = '';
  guardando = false;

  constructor(private adminService: AdminService) {}

  get productosFiltrados(): ProductoInventario[] {
    const termino = this.busqueda.trim().toLocaleLowerCase();
    if (!termino) return this.productos;

    return this.productos.filter((producto) =>
      producto.nombre.toLocaleLowerCase().includes(termino),
    );
  }

  abreviarUnidad(unidad: string): string {
    return unidad === 'UNIDAD' ? 'U' : unidad;
  }

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;

    this.adminService.inventario().subscribe({
      next: (p) => {
        this.productos = p;
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

  /** Nivel de stock de un producto: critico, bajo o normal. */
  nivel(p: ProductoInventario): 'critico' | 'bajo' | 'normal' {
    if (p.stockActual <= p.stockCritico) return 'critico';
    if (p.stockActual <= p.stockMinimo) return 'bajo';
    return 'normal';
  }

  /** Nombres de los productos en stock critico, para la alerta. */
  get nombresCriticos(): string {
    return this.criticos.map((p) => p.nombre).join(', ');
  }

  abrirFormulario(producto: ProductoInventario): void {
    this.productoId = producto.id;
    this.cantidad = 0;
    this.motivo = `Reabastecimiento de ${producto.nombre}`;
    this.mostrarFormulario = true;
    this.mensaje = '';
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
