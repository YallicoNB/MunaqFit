import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';

interface EnlaceMenu {
  ruta: string;
  texto: string;
  icono: string;
}

@Component({
  selector: 'app-sidebar',
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class Sidebar {
  @Input() esAdmin = false;
  @Output() cerrar = new EventEmitter<void>();

  enlacesEmpleado: EnlaceMenu[] = [
    { ruta: '/home', texto: 'Inicio', icono: '🏠' },
    { ruta: '/menu', texto: 'Menu', icono: '🥤' },
    { ruta: '/ordenes', texto: 'Ordenes', icono: '📋' },
    { ruta: '/cuenta', texto: 'Mi cuenta', icono: '👤' },
  ];

  enlacesAdmin: EnlaceMenu[] = [
    { ruta: '/dashboard', texto: 'Dashboard', icono: '📊' },
    { ruta: '/admin/inventario', texto: 'Inventario', icono: '📦' },
    { ruta: '/admin/usuarios', texto: 'Usuarios', icono: '🧑‍💼' },
    { ruta: '/admin/reportes', texto: 'Reportes', icono: '📈' },
  ];

  get enlaces(): EnlaceMenu[] {
    return this.esAdmin ? this.enlacesAdmin : this.enlacesEmpleado;
  }
}
