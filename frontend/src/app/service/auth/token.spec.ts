import { Token } from './token';

function fakeJwt(payload: Record<string, unknown>): string {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const body = btoa(JSON.stringify(payload))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '');
  return `${header}.${body}.firma`;
}

describe('Token', () => {
  let service: Token;

  beforeEach(() => {
    localStorage.clear();
    service = new Token();
  });

  afterEach(() => localStorage.clear());

  it('guarda y limpia access, refresh y datos de sesión', () => {
    service.guardarSesion({
      token: fakeJwt({ sub: '12345678', rol: 'ADMIN', exp: Math.floor(Date.now() / 1000) + 3600 }),
      refreshToken: fakeJwt({ sub: '12345678', tipo: 'refresh', exp: Math.floor(Date.now() / 1000) + 7200 }),
      tipo: 'Bearer',
      idUsuario: 1,
      dni: '12345678',
      nombreCompleto: 'Noe Munaq',
      email: 'noe@munaqfit.pe',
      rol: 'ADMIN',
      expiraEn: 900000,
    });

    expect(localStorage.getItem('token')).toBeTruthy();
    expect(localStorage.getItem('refreshToken')).toBeTruthy();
    expect(service.getNombre()).toBe('Noe Munaq');
    expect(service.getRol()).toBe('ADMIN');
    expect(service.haySesion()).toBeTrue();

    service.cerrarSesion();

    expect(localStorage.getItem('token')).toBeNull();
    expect(localStorage.getItem('refreshToken')).toBeNull();
    expect(service.haySesion()).toBeFalse();
  });

  it('considera vencido el access pero conserva el refresh', () => {
    localStorage.setItem('token', fakeJwt({ sub: '12345678', exp: 1 }));
    localStorage.setItem('refreshToken', fakeJwt({ sub: '12345678', exp: Math.floor(Date.now() / 1000) + 7200 }));

    expect(service.decodificarToken()).toBeNull();
    expect(service.decodificarPayload()?.sub).toBe('12345678');
    expect(service.haySesion()).toBeTrue();
  });

  it('no hay sesión si ambos tokens vencieron', () => {
    localStorage.setItem('token', fakeJwt({ sub: '12345678', exp: 1 }));
    localStorage.setItem('refreshToken', fakeJwt({ sub: '12345678', exp: 1 }));

    expect(service.haySesion()).toBeFalse();
  });
});
