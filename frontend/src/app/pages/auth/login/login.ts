import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Login as LoginService } from '../../../service/auth/login';
import { Token } from '../../../service/auth/token';
import { ToastService } from '../../../shared/toast/toast.service';

@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login implements OnInit {
  dni: string = '';
  password: string = '';
  /** Evita el doble envio y muestra el spinner mientras espera al backend. */
  cargando: boolean = false;
  /** Mensaje de error visible; cadena vacia cuando no hay fallo. */
  error: string = '';

  constructor(
    private loginService: LoginService,
    private token: Token,
    private router: Router,
    private toast: ToastService,
  ) {}

  ngOnInit(): void {
    // Arranca el backend antes de que el usuario pulse Ingresar.
    this.loginService.despertar();
  }

  iniciarSesion(): void {
    if (this.cargando) {
      return;
    }
    this.error = '';
    this.cargando = true;

    this.loginService.iniciarSesion(this.dni, this.password).subscribe({
      next: (respuesta) => {
        this.cargando = false;
        this.token.guardarSesion(respuesta);
        this.router.navigate(['/home']);
      },
      error: (e) => {
        this.cargando = false;
        // El backend ya devuelve un texto claro (401, bloqueo por intentos).
        this.error =
          e?.error?.message ?? 'No se pudo iniciar sesión. Revisa tu conexión e inténtalo de nuevo.';
        this.toast.error(this.error, 'Iniciar sesión');
      },
    });
  }
}