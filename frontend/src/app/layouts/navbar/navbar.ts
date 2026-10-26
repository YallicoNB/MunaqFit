import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { NavbarCuenta } from '../navbar-cuenta/navbar-cuenta';
import { Token } from '../../service/auth/token';

@Component({
  selector: 'app-navbar',
  imports: [CommonModule, RouterLink, RouterLinkActive, NavbarCuenta],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {
  @Output() cuentaAbiertaChange = new EventEmitter<boolean>();

  enlacesEmpleado = [
    { ruta: '/ventas', texto: 'Ventas' },
    { ruta: '/menu', texto: 'Menú' },
    { ruta: '/ordenes', texto: 'Órdenes' },
    { ruta: '/cuenta', texto: 'Cuenta' }
  ];

  enlacesAdmin = [
    { ruta: '/dashboard', texto: 'Dashboard' },
    { ruta: '/admin/inventario', texto: 'Inventario' },
    { ruta: '/admin/usuarios', texto: 'Usuarios' },
    { ruta: '/admin/reportes', texto: 'Reportes' },
    { ruta: '/admin/kardex', texto: 'Kardex' },
    { ruta: '/admin/ventas', texto: 'Ventas' },
    { ruta: '/admin/ordenes', texto: 'Órdenes' }
  ];

  cuentaAbierta = false;

  constructor(private token: Token) {}

  get esAdmin(): boolean {
    return localStorage.getItem('rolUsuario') === 'ADMIN';
  }

  get nombreUsuario(): string {
    return this.token.getNombre() || 'Usuario';
  }

  get enlaces(): { ruta: string; texto: string }[] {
    return this.esAdmin ? this.enlacesAdmin : this.enlacesEmpleado;
  }

  alternarCuenta(): void {
    this.cuentaAbierta = !this.cuentaAbierta;
    this.cuentaAbiertaChange.emit(this.cuentaAbierta);
  }

  cerrarCuenta(): void {
    this.cuentaAbierta = false;
    this.cuentaAbiertaChange.emit(false);
  }
}
