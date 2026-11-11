import { TestBed } from '@angular/core/testing';
import { UrlTree, provideRouter } from '@angular/router';
import { authGuard, guestGuard } from './auth-guard';

function fakeJwt(payload: Record<string, unknown>): string {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const body = btoa(JSON.stringify(payload)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${header}.${body}.firma`;
}

describe('authGuard / guestGuard', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
  });

  afterEach(() => localStorage.clear());

  it('permite entrar con access token vigente', () => {
    localStorage.setItem('token', fakeJwt({ sub: '12345678', rol: 'ADMIN', exp: Math.floor(Date.now() / 1000) + 3600 }));

    const resultado = TestBed.runInInjectionContext(() =>
      authGuard({} as never, { url: '/home' } as never)
    );

    expect(resultado).toBeTrue();
  });

  it('permite entrar con refresh válido aunque el access haya vencido', () => {
    localStorage.setItem('token', fakeJwt({ sub: '12345678', exp: 1 }));
    localStorage.setItem('refreshToken', fakeJwt({ sub: '12345678', exp: Math.floor(Date.now() / 1000) + 3600 }));

    const resultado = TestBed.runInInjectionContext(() =>
      authGuard({} as never, { url: '/home' } as never)
    );

    expect(resultado).toBeTrue();
  });

  it('redirige al login sin sesión', () => {
    const resultado = TestBed.runInInjectionContext(() =>
      authGuard({} as never, { url: '/home' } as never)
    );

    expect(resultado instanceof UrlTree).toBeTrue();
  });

  it('guestGuard redirige a home si ya hay sesión', () => {
    localStorage.setItem('token', fakeJwt({ sub: '12345678', exp: Math.floor(Date.now() / 1000) + 3600 }));

    const resultado = TestBed.runInInjectionContext(() =>
      guestGuard({} as never, { url: '/' } as never)
    );

    expect(resultado instanceof UrlTree).toBeTrue();
  });
});
