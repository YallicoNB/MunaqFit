import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Login as LoginService } from '../../../service/auth/login';
import { Token } from '../../../service/auth/token';
import { ToastService } from '../../../shared/toast/toast.service';
import { CampoInput } from '../../../shared/input/input';
import { Boton } from '../../../shared/boton/boton';

@Component({
  selector: 'app-login',
  imports: [CommonModule, ReactiveFormsModule, RouterLink, CampoInput, Boton],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login implements OnInit {
  formulario: FormGroup;
  /** Evita el doble envio y muestra el spinner mientras espera al backend. */
  cargando = false;
  /** Mensaje de error visible; cadena vacia cuando no hay fallo. */
  error = '';

  constructor(
    private fb: FormBuilder,
    private loginService: LoginService,
    private token: Token,
    private router: Router,
    private toast: ToastService,
  ) {
    this.formulario = this.fb.group({
      dni: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
      password: ['', [Validators.required, Validators.minLength(8)]],
    });
  }

  ngOnInit(): void {
    // Arranca el backend antes de que el usuario pulse Ingresar.
    this.loginService.despertar();
  }

  get dni() {
    return this.formulario.get('dni')!;
  }

  get password() {
    return this.formulario.get('password')!;
  }

  iniciarSesion(): void {
    if (this.cargando) {
      return;
    }

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.error = '';
    this.cargando = true;

    const { dni, password } = this.formulario.value;

    this.loginService.iniciarSesion(dni, password).subscribe({
      next: (respuesta) => {
        this.cargando = false;
        this.token.guardarSesion(respuesta);
        this.router.navigate(['/home']);
      },
      error: (e) => {
        this.cargando = false;
        this.error =
          e?.error?.message ?? 'No se pudo iniciar sesión. Revisa tu conexión e inténtalo de nuevo.';
        this.toast.error(this.error, 'Iniciar sesión');
      },
    });
  }
}
