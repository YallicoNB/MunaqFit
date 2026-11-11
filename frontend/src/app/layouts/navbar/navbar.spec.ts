import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Navbar } from './navbar';

describe('Navbar', () => {
  let component: Navbar;
  let fixture: ComponentFixture<Navbar>;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [Navbar],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Navbar);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => localStorage.clear());

  it('muestra los enlaces de administrador cuando el rol es ADMIN', () => {
    localStorage.setItem('rolUsuario', 'ADMIN');
    fixture.detectChanges();

    const textos = component.enlaces.map((e) => e.texto);
    expect(component.esAdmin).toBeTrue();
    expect(textos).toContain('Dashboard');
    expect(textos).toContain('Inventario');
    expect(textos).toContain('Usuarios');
    expect(textos).toContain('Reportes');
    expect(textos).toContain('Kardex');
  });

  it('muestra los enlaces de empleado cuando el rol es EMPLEADO', () => {
    localStorage.setItem('rolUsuario', 'EMPLEADO');
    fixture.detectChanges();

    const textos = component.enlaces.map((e) => e.texto);
    expect(component.esAdmin).toBeFalse();
    expect(textos).toContain('Ventas');
    expect(textos).toContain('Menú');
    expect(textos).toContain('Órdenes');
    expect(textos).toContain('Cuenta');
  });
});
