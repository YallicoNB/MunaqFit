import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Card } from '../../components/card/card';
import { VentaService } from '../../service/venta.service';
import { Bebida } from '../../models/producto';

interface OpcionPrecio {
  etiqueta: string;
  minimo: number | null;
}

@Component({
  selector: 'app-menu',
  imports: [CommonModule, FormsModule, Card],
  templateUrl: './menu.html',
  styleUrl: './menu.css',
})
export class Menu implements OnInit {
  bebidas: Bebida[] = [];
  cargando = true;
  error = '';
  busqueda = '';
  readonly opcionesPrecio: OpcionPrecio[] = [
    { etiqueta: 'Cualquier precio', minimo: null },
    { etiqueta: 'S/ 10 o más', minimo: 10 },
    { etiqueta: 'S/ 15 o más', minimo: 15 },
    { etiqueta: 'S/ 20 o más', minimo: 20 },
  ];
  precioMinimo: number | null = null;
  categoriaSeleccionada: string | null = null;

  get categorias(): string[] {
    return [...new Set(this.bebidas.map((bebida) => bebida.categoria.trim()).filter(Boolean))]
      .sort((a, b) => a.localeCompare(b, 'es'));
  }

  get bebidasFiltradas(): Bebida[] {
    const nombreBuscado = this.busqueda.trim().toLocaleLowerCase('es');
    return this.bebidas.filter((bebida) => {
      const coincideNombre = bebida.nombre.toLocaleLowerCase('es').includes(nombreBuscado);
      const coincideCategoria =
        !this.categoriaSeleccionada ||
        bebida.categoria.trim() === this.categoriaSeleccionada;
      const coincidePrecioMinimo =
        this.precioMinimo == null || bebida.precio >= this.precioMinimo;

      return coincideNombre && coincideCategoria && coincidePrecioMinimo;
    });
  }

  constructor(private ventaService: VentaService) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.ventaService.menu().subscribe({
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

  seleccionarCategoria(categoria: string | null): void {
    this.categoriaSeleccionada = categoria;
  }

  seleccionarPrecio(minimo: number | null): void {
    this.precioMinimo = minimo;
  }

  limpiarFiltros(): void {
    this.busqueda = '';
    this.precioMinimo = null;
    this.categoriaSeleccionada = null;
  }

  iconoCategoria(categoria: string): string {
    const nombre = categoria.toLocaleLowerCase('es');
    if (nombre.includes('cafe') || nombre.includes('café')) return '☕';
    if (nombre.includes('jugo') || nombre.includes('zumo')) return '🧃';
    if (nombre.includes('te') || nombre.includes('té')) return '🍵';
    if (nombre.includes('agua')) return '💧';
    if (nombre.includes('smoothie') || nombre.includes('batido')) return '🥤';
    return '🍹';
  }
}
