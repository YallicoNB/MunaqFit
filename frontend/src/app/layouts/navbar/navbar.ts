import { Component, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
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
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {
  private readonly rol = signal<string | null>(null);

  readonly esAdmin = computed(() => this.rol() === 'ADMIN');

  readonly enlaces = computed<EnlaceNav[]>(() => {
    const propios = this.esAdmin() ? ENLACES_ADMIN : ENLACES_EMPLEADO;
    return [...ENLACES_COMUNES, ...propios];
  });

  readonly nombreUsuario = signal('');

  constructor(token: Token) {
    this.rol.set(token.getRol());
    this.nombreUsuario.set(token.getNombre() ?? '');
  }
}
