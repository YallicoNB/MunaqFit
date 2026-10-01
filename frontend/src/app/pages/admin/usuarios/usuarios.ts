import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../service/admin.service';
import { Rol, Usuario } from '../../../models/admin';
import { Token } from '../../../service/auth/token';

@Component({
  selector: 'app-usuarios',
  imports: [CommonModule, FormsModule],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.css',
})
export class Usuarios implements OnInit {
  usuarios: Usuario[] = [];
  cargando = true;
  error = '';
  mensaje = '';

  // Formulario de alta. El backend siempre crea al usuario con rol EMPLEADO.
  mostrarFormulario = false;
  dni = '';
  nombreCompleto = '';
  email = '';
  password = '';
  guardando = false;

  constructor(
    private adminService: AdminService,
    private token: Token
  ) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.adminService.listarUsuarios().subscribe({
      next: (u) => {
        this.usuarios = u;
        this.cargando = false;
      },
      error: () => {
        this.error = 'No se pudieron cargar los usuarios';
        this.cargando = false;
      },
    });
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

  /** El backend lo marca como INACTIVO, no lo borra de la base de datos. */
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

  /** Marca si la fila es del usuario con la sesión iniciada. */
  esMiUsuario(usuario: Usuario): boolean {
    return usuario.dni === this.token.getDni();
  }

  /** Cambia el rol de un usuario. Si el backend lo rechaza, la UI no cambia. */
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
