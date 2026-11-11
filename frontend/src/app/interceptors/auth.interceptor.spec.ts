import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { AuthInterceptor } from './auth.interceptor';

describe('AuthInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([AuthInterceptor])),
        provideHttpClientTesting(),
      ],
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('agrega el header Authorization a las peticiones privadas', () => {
    localStorage.setItem('token', 'access-viejo');
    localStorage.setItem('refreshToken', 'refresh-viejo');

    http.get('/api/admin/usuarios').subscribe();

    const req = httpMock.expectOne('/api/admin/usuarios');
    expect(req.request.headers.get('Authorization')).toBe('Bearer access-viejo');
    req.flush({});
  });

  it('ante un 401 renueva el token y reintenta la petición', () => {
    localStorage.setItem('token', 'access-viejo');
    localStorage.setItem('refreshToken', 'refresh-viejo');

    let respuesta: unknown;
    http.get('/api/admin/usuarios').subscribe((r) => (respuesta = r));

    const original = httpMock.expectOne('/api/admin/usuarios');
    original.flush({}, { status: 401, statusText: 'Unauthorized' });

    const refresh = httpMock.expectOne((r) => r.url.includes('/api/auth/refresh'));
    expect(refresh.request.body).toEqual({ refreshToken: 'refresh-viejo' });
    refresh.flush({
      token: 'access-nuevo',
      refreshToken: 'refresh-nuevo',
      tipo: 'Bearer',
      idUsuario: 1,
      dni: '12345678',
      nombreCompleto: 'Noe Munaq',
      email: 'noe@munaqfit.pe',
      rol: 'ADMIN',
      expiraEn: 900000,
    });

    const reintento = httpMock.expectOne('/api/admin/usuarios');
    expect(reintento.request.headers.get('Authorization')).toBe('Bearer access-nuevo');
    reintento.flush({ ok: true });

    expect(respuesta).toEqual({ ok: true });
    expect(localStorage.getItem('token')).toBe('access-nuevo');
    expect(localStorage.getItem('refreshToken')).toBe('refresh-nuevo');
  });

  it('si no hay refresh, limpia la sesión y vuelve al login', () => {
    localStorage.setItem('token', 'access-viejo');
    const navegar = spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));

    http.get('/api/admin/usuarios').subscribe({ error: () => undefined });

    const original = httpMock.expectOne('/api/admin/usuarios');
    original.flush({}, { status: 401, statusText: 'Unauthorized' });

    httpMock.expectNone((r) => r.url.includes('/api/auth/refresh'));
    expect(localStorage.getItem('token')).toBeNull();
    expect(navegar).toHaveBeenCalledWith(['/']);
  });
});
