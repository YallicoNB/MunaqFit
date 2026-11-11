import { TestBed } from '@angular/core/testing';
import { UrlTree, provideRouter } from '@angular/router';
import { adminGuard, empleadoGuard } from './role.guard';

function fakeJwt(payload: Record<string, unknown>): string {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const body = btoa(JSON.stringify(payload)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${header}.${body}.firma`;
}

describe('roleGuard', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
  });

  afterEach(() => localStorage.clear());

  it('ADMIN puede entrar a rutas de admin', () => {
    localStorage.setItem('token', fakeJwt({ sub: '1', rol: 'ADMIN', exp: Math.floor(Date.now() / 1000) + 3600 }));

    const resultado = TestBed.runInInjectionContext(() =>
      adminGuard({} as never, { url: '/admin/usuarios' } as never)
    );

    expect(resultado).toBeTrue();
  });

  it('EMPLEADO no puede entrar a rutas de admin', () => {
    localStorage.setItem('token', fakeJwt({ sub: '2', rol: 'EMPLEADO', exp: Math.floor(Date.now() / 1000) + 3600 }));

    const resultado = TestBed.runInInjectionContext(() =>
      adminGuard({} as never, { url: '/admin/usuarios' } as never)
    );

    expect(resultado instanceof UrlTree).toBeTrue();
  });

  it('EMPLEADO sí puede entrar a rutas de empleado', () => {
    localStorage.setItem('token', fakeJwt({ sub: '2', rol: 'EMPLEADO', exp: Math.floor(Date.now() / 1000) + 3600 }));

    const resultado = TestBed.runInInjectionContext(() =>
      empleadoGuard({} as never, { url: '/menu' } as never)
    );

    expect(resultado).toBeTrue();
  });
});
