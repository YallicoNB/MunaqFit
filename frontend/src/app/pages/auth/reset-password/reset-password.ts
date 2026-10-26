import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../service/auth/auth.service';
import { ToastService } from '../../../shared/toast/toast.service';

@Component({
  selector: 'app-reset-password',
  imports: [CommonModule, FormsModule],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.css',
})
export class ResetPassword implements OnInit {
  nuevaContrasena: string = '';
  token: string = '';
  cargando: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe((params: any) => {
      this.token = params['token'] || '';
    });
  }

  cambiarContrasena(): void {
    if (!this.nuevaContrasena || !this.token) return;
    this.cargando = true;

    this.authService.cambiarContrasena(this.token, this.nuevaContrasena).subscribe({
      next: () => {
        this.toastService.success('Contraseña actualizada correctamente');
        this.router.navigate(['/auth/login']);
      },
      error: () => {
        this.toastService.error('Enlace inválido o expirado');
        this.cargando = false;
      }
    });
  }
}
