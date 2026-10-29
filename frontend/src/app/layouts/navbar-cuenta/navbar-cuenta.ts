import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';

interface EnlaceCuenta {
  ruta: string;
  texto: string;
}

@Component({
  selector: 'app-navbar-cuenta',
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './navbar-cuenta.html',
  styleUrl: './navbar-cuenta.css',
})
export class NavbarCuenta {
  @Input() abierto = false;
  @Input() esAdmin = false;
  @Output() cerrar = new EventEmitter<void>();

  private readonly enlacesEmpleado: EnlaceCuenta[] = [
    { ruta: '/ventas', texto: 'Ventas' },
    { ruta: '/menu', texto: 'Menú' },
    { ruta: '/ordenes', texto: 'Órdenes' },
    { ruta: '/cuenta', texto: 'Cuenta' },
  ];

  private readonly enlacesAdmin: EnlaceCuenta[] = [
    { ruta: '/admin/dashboard', texto: 'Dashboard' },
    { ruta: '/admin/inventario', texto: 'Inventario' },
    { ruta: '/admin/usuarios', texto: 'Usuarios' },
    { ruta: '/admin/reportes', texto: 'Reportes' },
    { ruta: '/admin/kardex', texto: 'Kardex' }, // Faltaba en tu arreglo original
    { ruta: '/ventas', texto: 'Ventas' }, // Asumiendo que el admin usa el mismo POS
    { ruta: '/admin/ordenes', texto: 'Órdenes' }, // Nueva ruta de la Tarea 3.3
  ];

  get enlaces(): EnlaceCuenta[] {
    return this.esAdmin ? this.enlacesAdmin : this.enlacesEmpleado;
  }

  cerrarMenu(): void {
    this.cerrar.emit();
  }
}
