import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  AbstractControl,
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../service/auth/auth.service';
import { CampoInput } from '../../../shared/input/input';

/** Valida que las dos contraseñas coincidan (error a nivel de grupo). */
export function passwordsIguales(grupo: AbstractControl): ValidationErrors | null {
  const nueva = grupo.get('nuevaPassword')?.value ?? '';
  const confirmar = grupo.get('confirmarPassword')?.value ?? '';
  if (!nueva && !confirmar) return null;
  return nueva === confirmar ? null : { noCoinciden: true };
}

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, CampoInput],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.css'
})
export class ResetPassword implements OnInit {
  token = '';
  formulario: FormGroup;
  cargando = false;
  mensajeError = '';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {
    this.formulario = this.fb.group(
      {
        nuevaPassword: ['', [Validators.required, Validators.minLength(8)]],
        confirmarPassword: ['', [Validators.required]],
      },
      { validators: passwordsIguales }
    );
  }

  get nuevaPassword() {
    return this.formulario.get('nuevaPassword')!;
  }

  get confirmarPassword() {
    return this.formulario.get('confirmarPassword')!;
  }

  get errorConfirmar(): string {
    if (!this.confirmarPassword.touched) return '';
    if (this.confirmarPassword.hasError('required')) return 'Debes confirmar la contraseña';
    if (this.formulario.hasError('noCoinciden')) return 'Las contraseñas no coinciden';
    return '';
  }

  ngOnInit(): void {
    // Captura el ?token= de la URL.
    this.route.queryParamMap.subscribe(params => {
      this.token = params.get('token') || '';
      if (!this.token) {
        this.mensajeError = 'Enlace inválido. Por favor, solicita uno nuevo.';
      }
    });
  }

  cambiarPassword(): void {
    this.mensajeError = '';

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.cargando = true;

    this.authService.resetearPassword(this.token, this.nuevaPassword.value).subscribe({
      next: () => {
        this.cargando = false;
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.cargando = false;
        this.mensajeError = err.error?.message || 'Error al restablecer la contraseña. El enlace puede haber expirado.';
      }
    });
  }
}
