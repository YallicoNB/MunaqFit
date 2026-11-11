import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../service/auth/auth.service';
import { CampoInput } from '../../../shared/input/input';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, CampoInput],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.css'
})
export class ForgotPassword {
  formulario: FormGroup;
  cargando = false;
  mensajeExito = '';
  mensajeError = '';

  constructor(private fb: FormBuilder, private authService: AuthService) {
    this.formulario = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
    });
  }

  get email() {
    return this.formulario.get('email')!;
  }

  solicitarRecuperacion(): void {
    if (this.cargando) return;

    this.formulario.patchValue(
      { email: String(this.formulario.value.email ?? '').trim() },
      { emitEvent: false }
    );

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const email = String(this.formulario.value.email);
    this.cargando = true;
    this.mensajeExito = '';
    this.mensajeError = '';

    this.authService.recuperarPassword(email).subscribe({
      next: () => {
        this.cargando = false;
        // El backend devuelve 200 exista o no el correo por seguridad.
        this.mensajeExito = 'Si el correo está registrado, hemos enviado las instrucciones.';
        this.formulario.reset({ email: '' });
      },
      error: (err) => {
        this.cargando = false;
        this.mensajeError = 'Ocurrió un error al procesar la solicitud.';
        console.error(err);
      }
    });
  }
}
