import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../service/auth/auth.service';


@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reset-password.html'
})
export class ResetPassword implements OnInit {
  token: string = '';
  nuevaPassword: string = '';
  confirmarPassword: string = '';
  cargando: boolean = false;
  mensajeError: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit() {
    // Captura el ?token= de la URL
    this.route.queryParamMap.subscribe(params => {
      this.token = params.get('token') || '';
      if (!this.token) {
        this.mensajeError = 'Enlace inválido. Por favor, solicita uno nuevo.';
      }
    });
  }

  cambiarPassword() {
    this.mensajeError = '';

    if (this.nuevaPassword.length < 8) {
      this.mensajeError = 'La contraseña debe tener mínimo 8 caracteres.';
      return;
    }

    if (this.nuevaPassword !== this.confirmarPassword) {
      this.mensajeError = 'Las contraseñas no coinciden.';
      return;
    }

    this.cargando = true;

    this.authService.resetearPassword(this.token, this.nuevaPassword).subscribe({
      next: () => {
        this.cargando = false;
        // Si tienes el ToastService, aquí iría un ToastService.exito('Contraseña actualizada')
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.cargando = false;
        this.mensajeError = err.error?.message || 'Error al restablecer la contraseña. El enlace puede haber expirado.';
      }
    });
  }
}
