import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../service/auth/auth.service';
import { ToastService } from '../../../shared/toast/toast.service';

@Component({
  selector: 'app-forgot-password',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.css',
})
export class ForgotPassword {
  correo: string = '';
  cargando: boolean = false;

  constructor(private authService: AuthService, private toastService: ToastService) {}

  enviarSolicitud() {
    if (!this.correo) return;
    this.cargando = true;
    this.authService.solicitarRecuperacion(this.correo).subscribe({
      next: () => {
        this.toastService.success('Instrucciones enviadas a tu correo');
        this.cargando = false;
      },
      error: () => {
        this.toastService.error('Error al solicitar recuperación');
        this.cargando = false;
      }
    });
  }
}
