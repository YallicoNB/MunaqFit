import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { Login } from './login';
import { Login as LoginService } from '../../../service/auth/login';
import { Token } from '../../../service/auth/token';

describe('Login', () => {
  let component: Login;
  let loginService: LoginService;
  let token: Token;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    loginService = TestBed.inject(LoginService);
    token = TestBed.inject(Token);
    router = TestBed.inject(Router);
    spyOn(loginService, 'despertar');
  });

  function crearComponente(): void {
    const fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('no llama al backend si el formulario está vacío', () => {
    crearComponente();
    spyOn(loginService, 'iniciarSesion');

    component.iniciarSesion();

    expect(loginService.iniciarSesion).not.toHaveBeenCalled();
    expect(component.formulario.touched).toBeTrue();
    expect(component.dni.invalid).toBeTrue();
    expect(component.password.invalid).toBeTrue();
  });

  it('rechaza un DNI que no tiene 8 dígitos', () => {
    crearComponente();
    spyOn(loginService, 'iniciarSesion');
    component.formulario.setValue({ dni: '1234', password: '12345678' });

    component.iniciarSesion();

    expect(loginService.iniciarSesion).not.toHaveBeenCalled();
    expect(component.dni.hasError('pattern')).toBeTrue();
  });

  it('con datos válidos guarda la sesión y navega a home', () => {
    crearComponente();
    const respuesta = {
      token: 'access',
      refreshToken: 'refresh',
      tipo: 'Bearer',
      idUsuario: 1,
      dni: '12345678',
      nombreCompleto: 'Noe Munaq',
      email: 'noe@munaqfit.pe',
      rol: 'ADMIN',
      expiraEn: 900000,
    };
    const iniciar = spyOn(loginService, 'iniciarSesion').and.returnValue(of(respuesta));
    const guardar = spyOn(token, 'guardarSesion');
    const navegar = spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));

    component.formulario.setValue({ dni: '12345678', password: '12345678' });
    component.iniciarSesion();

    expect(iniciar).toHaveBeenCalledWith('12345678', '12345678');
    expect(guardar).toHaveBeenCalledWith(respuesta);
    expect(navegar).toHaveBeenCalledWith(['/home']);
  });
});
