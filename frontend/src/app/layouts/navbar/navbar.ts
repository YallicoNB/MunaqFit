
import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { NavbarCuenta } from '../navbar-cuenta/navbar-cuenta';
import { Token } from '../../service/auth/token';

interface EnlaceNav {
  ruta: string;
  texto: string;
}

/** Enlaces que ve cualquier usuario con sesion (admin o empleado). */
const ENLACES_COMUNES: EnlaceNav[] = [
  { ruta: '/home', texto: 'Inicio' },
  { ruta: '/ventas', texto: 'Punto de venta' },
  { ruta: '/cuenta', texto: 'Mi cuenta' },
];

/** Rutas que solo puede abrir un ADMIN. */
const ENLACES_ADMIN: EnlaceNav[] = [
  { ruta: '/dashboard', texto: 'Dashboard' },
  { ruta: '/admin/inventario', texto: 'Inventario' },
  { ruta: '/admin/usuarios', texto: 'Usuarios' },
  { ruta: '/admin/reportes', texto: 'Reportes' },
  { ruta: '/empleados', texto: 'Empleados' },
];

/** Rutas que solo puede abrir un EMPLEADO. */
const ENLACES_EMPLEADO: EnlaceNav[] = [
  { ruta: '/menu', texto: 'Menu' },
  { ruta: '/ordenes', texto: 'Ordenes' },
];

/**
 * Barra de navegacion superior. Muestra solo los destinos que el rol actual
 * puede abrir de verdad, para no poner enlaces que el guard va a rebotar.
 */
@Component({
  selector: 'app-navbar',

  imports: [CommonModule, RouterLink, RouterLinkActive, NavbarCuenta],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {

  @Output() cuentaAbiertaChange = new EventEmitter<boolean>();

  enlacesEmpleado = [
    { ruta: '/home', texto: 'Inicio' },
    { ruta: '/menu', texto: 'Menú' },
  ];

  enlacesAdmin = [
    { ruta: '/home', texto: 'Inicio' },
    { ruta: '/menu', texto: 'Menú' },
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
