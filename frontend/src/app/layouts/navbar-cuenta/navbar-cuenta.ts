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
    { ruta: '/menu', texto: 'Menú' },
    { ruta: '/ordenes', texto: 'Órdenes' },
    { ruta: '/ventas', texto: 'Punto de venta' },
    { ruta: '/cuenta', texto: 'Mi cuenta' },
  ];

  private readonly enlacesAdmin: EnlaceCuenta[] = [
    { ruta: '/dashboard', texto: 'Dashboard' },
    { ruta: '/menu', texto: 'Menú' },
    { ruta: '/ordenes', texto: 'Órdenes' },
    { ruta: '/ventas', texto: 'Punto de venta' },
    { ruta: '/admin/inventario', texto: 'Inventario' },
    { ruta: '/admin/usuarios', texto: 'Empleados' },
    { ruta: '/admin/reportes', texto: 'Reportes' },
    { ruta: '/cuenta', texto: 'Mi cuenta' },
  ];

  get enlaces(): EnlaceCuenta[] {
    return this.esAdmin ? this.enlacesAdmin : this.enlacesEmpleado;
  }

  cerrarMenu(): void {
    this.cerrar.emit();
  }
}
