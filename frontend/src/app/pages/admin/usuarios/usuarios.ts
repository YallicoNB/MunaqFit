import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../service/admin.service';
import { Usuario } from '../../../models/admin';

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

  constructor(private adminService: AdminService) {}

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
}
