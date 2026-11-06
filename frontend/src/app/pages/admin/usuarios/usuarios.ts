import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../service/admin.service';
import { Rol, Usuario, PageResponse } from '../../../models/admin';
import { Token } from '../../../service/auth/token';
import { Boton } from '../../../shared/boton/boton';
import { Modal } from '../../../shared/modal/modal';
import { Badge } from '../../../shared/badge/badge';
import { Spinner } from '../../../shared/spinner/spinner';
import { EstadoVacio } from '../../../shared/estado-vacio/estado-vacio';

@Component({
  selector: 'app-usuarios',
  imports: [CommonModule, FormsModule, Boton, Modal, Badge, Spinner, EstadoVacio],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.css',
})
export class Usuarios implements OnInit {
  pagina: PageResponse<Usuario> | null = null;
  cargando = true;
  error = '';
  mensaje = '';

  //Filtros
  terminoBusqueda = '';
  estadoFiltro = '';
  paginaActual = 0;
  tamano = 20;

  //El backend siempre crea al usuario con rol EMPLEADO.
  mostrarFormulario = false;
  dni = '';
  nombreCompleto = '';
  email = '';
  password = '';
  guardando = false;

  columnas: { clave: string; titulo: string }[] = [];

  constructor(
    private adminService: AdminService,
    private token: Token
  ) {}

  get usuarios(): Usuario[] {
    return this.pagina?.contenido ?? [];
  }

  get totalPaginas(): number {
    return this.pagina?.totalPaginas ?? 0;
  }

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.adminService.listarUsuarios(this.paginaActual, this.tamano, this.terminoBusqueda, this.estadoFiltro).subscribe({
      next: (p) => {
        this.pagina = p;
        this.cargando = false;
      },
      error: () => {
        this.error = 'No se pudieron cargar los usuarios';
        this.cargando = false;
      },
    });
  }

  aplicarFiltros(): void {
    this.paginaActual = 0;
    this.cargar();
  }

  irPagina(p: number): void {
    if (p < 0 || p >= this.totalPaginas) return;
    this.paginaActual = p;
    this.cargar();
  }

  abrirFormulario(): void {
    this.dni = '';
    this.nombreCompleto = '';
    this.email = '';
    this.password = '';
    this.mostrarFormulario = true;
    this.error = '';
    this.mensaje = '';
  }

  crear(): void {
    if (!this.dni || !this.nombreCompleto || !this.email || !this.password) {
      this.error = 'Completa todos los campos obligatorios';
      return;
    }

    this.guardando = true;
    this.error = '';

    this.adminService
      .crearUsuario({
        dni: this.dni,
        nombreCompleto: this.nombreCompleto,
        email: this.email,
        password: this.password,
      })
      .subscribe({
        next: (u) => {
          this.mensaje = `Usuario ${u.nombreCompleto} creado correctamente`;
          this.guardando = false;
          this.mostrarFormulario = false;
          this.cargar();
        },
        error: (e) => {
          this.error = e?.error?.message ?? e?.error ?? 'No se pudo crear el usuario';
          this.guardando = false;
        },
      });
  }

  desactivar(usuario: Usuario): void {
    if (!confirm(`Inactivar a ${usuario.nombreCompleto}?`)) return;

    this.adminService.eliminarUsuario(usuario.id!).subscribe({
      next: () => {
        this.mensaje = `Usuario ${usuario.nombreCompleto} inactivado`;
        this.cargar();
      },
      error: (e) => (this.error = e?.error ?? 'No se pudo inactivar el usuario'),
    });
  }

  esMiUsuario(usuario: Usuario): boolean {
    return usuario.dni === this.token.getDni();
  }

  cambiarRol(usuario: Usuario, nuevoRol: Rol): void {
    if (usuario.rol === nuevoRol) return;

    this.error = '';
    this.mensaje = '';

    this.adminService.cambiarRol(usuario.id!, nuevoRol).subscribe({
      next: (u) => {
        usuario.rol = u.rol;
        this.mensaje = `Rol de ${u.nombreCompleto} actualizado a ${u.rol}`;
      },
      error: (e) => {
        this.error = e?.error ?? 'No se pudo cambiar el rol';
      },
    });
  }
}
