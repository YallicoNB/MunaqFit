import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ForgotPassword } from './forgot-password';
import { AuthService } from '../../../service/auth/auth.service';

describe('ForgotPassword', () => {
  let component: ForgotPassword;
  let authService: AuthService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ForgotPassword],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    authService = TestBed.inject(AuthService);
  });

  function crearComponente(): void {
    const fixture = TestBed.createComponent(ForgotPassword);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('no llama al backend con el correo vacío', () => {
    crearComponente();
    const recuperar = spyOn(authService, 'recuperarPassword');

    component.solicitarRecuperacion();

    expect(recuperar).not.toHaveBeenCalled();
    expect(component.email.hasError('required')).toBeTrue();
    expect(component.formulario.touched).toBeTrue();
  });

  it('rechaza un correo sin formato válido', () => {
    crearComponente();
    const recuperar = spyOn(authService, 'recuperarPassword');
    component.formulario.setValue({ email: 'no-es-correo' });

    component.solicitarRecuperacion();

    expect(recuperar).not.toHaveBeenCalled();
    expect(component.email.hasError('email')).toBeTrue();
  });

  it('envía el correo sin espacios y limpia el formulario', () => {
    crearComponente();
    const recuperar = spyOn(authService, 'recuperarPassword').and.returnValue(of({}));
    component.formulario.setValue({ email: '  noe@munaqfit.pe  ' });

    component.solicitarRecuperacion();

    expect(recuperar).toHaveBeenCalledWith('noe@munaqfit.pe');
    expect(component.mensajeExito).toContain('Si el correo está registrado');
    expect(component.formulario.value.email).toBe('');
  });
});
