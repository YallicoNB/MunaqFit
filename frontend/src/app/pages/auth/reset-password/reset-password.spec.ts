import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ResetPassword } from './reset-password';
import { AuthService } from '../../../service/auth/auth.service';

describe('ResetPassword', () => {
  let component: ResetPassword;
  let authService: AuthService;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResetPassword],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: { queryParamMap: of(convertToParamMap({ token: 'token-valido' })) },
        },
      ],
    }).compileComponents();

    authService = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
  });

  function crearComponente(): void {
    const fixture = TestBed.createComponent(ResetPassword);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('toma el token de la URL', () => {
    crearComponente();
    expect(component.token).toBe('token-valido');
  });

  it('no envía nada si las contraseñas no coinciden', () => {
    crearComponente();
    const resetear = spyOn(authService, 'resetearPassword');
    component.formulario.setValue({ nuevaPassword: '12345678', confirmarPassword: '87654321' });

    component.cambiarPassword();

    expect(resetear).not.toHaveBeenCalled();
    expect(component.formulario.hasError('noCoinciden')).toBeTrue();
    expect(component.formulario.touched).toBeTrue();
  });

  it('rechaza contraseñas de menos de 8 caracteres', () => {
    crearComponente();
    const resetear = spyOn(authService, 'resetearPassword');
    component.formulario.setValue({ nuevaPassword: 'corta', confirmarPassword: 'corta' });

    component.cambiarPassword();

    expect(resetear).not.toHaveBeenCalled();
    expect(component.nuevaPassword.hasError('minlength')).toBeTrue();
  });

  it('con contraseñas válidas e iguales llama al backend y navega al login', () => {
    crearComponente();
    const resetear = spyOn(authService, 'resetearPassword').and.returnValue(of({}));
    const navegar = spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));
    component.formulario.setValue({ nuevaPassword: '12345678', confirmarPassword: '12345678' });

    component.cambiarPassword();

    expect(resetear).toHaveBeenCalledWith('token-valido', '12345678');
    expect(navegar).toHaveBeenCalledWith(['/login']);
  });
});
