import { Component, OnInit } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { VentaService } from '../../service/venta.service';
import { ProductoService } from '../../service/producto.service';
import { Navbar } from '../../layouts/navbar/navbar';
import { Bebida, Receta } from '../../models/producto';

@Component({
  selector: 'app-menu',
  imports: [CommonModule, DecimalPipe, Navbar],
  templateUrl: './menu.html',
  styleUrl: './menu.css',
})
export class Menu implements OnInit {
  bebidas: Bebida[] = [];
  cargando = true;
  error = '';

  /** Insumos de la bebida abierta, para ver como se prepara. */
  recetaAbierta: Receta[] | null = null;
  recetaDeBebida = '';

  constructor(
    private ventaService: VentaService,
    private productoService: ProductoService
  ) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.ventaService.getBebidas().subscribe({
      next: (bebidas) => {
        this.bebidas = bebidas;
        this.cargando = false;
      },
      error: () => {
        this.error = 'No se pudo cargar el menu de bebidas';
        this.cargando = false;
      },
    });
  }

  /** Abre o cierra la lista de insumos de una bebida. */
  verReceta(bebida: Bebida): void {
    if (this.recetaDeBebida === bebida.nombre) {
      this.recetaAbierta = null;
      this.recetaDeBebida = '';
      return;
    }

    this.recetaDeBebida = bebida.nombre;
    this.recetaAbierta = null;

    this.productoService.getRecetasByBebidaId(bebida.id).subscribe({
      next: (recetas) => (this.recetaAbierta = recetas),
      error: () => (this.recetaAbierta = []),
    });
  }
}
