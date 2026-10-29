import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../service/auth/auth.service';

// Importa tu ToastService si lo tienes configurado, o usa el manejo de errores básico

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './forgot-password.html'
})
export class ForgotPassword {
  email: string = '';
  cargando: boolean = false;
  mensajeExito: string = '';
  mensajeError: string = '';

  constructor(private authService: AuthService) {}

  solicitarRecuperacion() {
    if (!this.email) return;

    this.cargando = true;
    this.mensajeExito = '';
    this.mensajeError = '';

    this.authService.recuperarPassword(this.email).subscribe({
      next: () => {
        this.cargando = false;
        // El backend devuelve 200 exista o no el correo por seguridad
        this.mensajeExito = 'Si el correo está registrado, hemos enviado las instrucciones.';
        this.email = '';
      },
      error: (err) => {
        this.cargando = false;
        this.mensajeError = 'Ocurrió un error al procesar la solicitud.';
        console.error(err);
      }
    });
  }
}
