import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Card } from './card';

describe('Card', () => {
  let fixture: ComponentFixture<Card>;
  let component: Card;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [Card] }).compileComponents();
    fixture = TestBed.createComponent(Card);
    component = fixture.componentInstance;
  });

  it('muestra la imagen de la bebida y su nombre alternativo', () => {
    component.bebida = {
      id: 1,
      nombre: 'Atomic',
      descripcion: 'Sabor a naranja',
      precio: 16,
      categoria: 'Energizante',
      imagenUrl: 'assets/bebidas/atomic.jpg',
      tiempoPreparacion: 3,
      activo: true,
    };
    component.accionable = false;
    fixture.detectChanges();

    const img: HTMLImageElement = fixture.nativeElement.querySelector('img.producto-imagen');
    expect(img).toBeTruthy();
    expect(img.getAttribute('src')).toBe('assets/bebidas/atomic.jpg');
    expect(img.getAttribute('alt')).toBe('Atomic');
  });

  it('muestra un fallback visual cuando la bebida no tiene foto', () => {
    component.bebida = {
      id: 2,
      nombre: 'PRE ENTRENO',
      descripcion: 'Con cafeína',
      precio: 16,
      categoria: 'Energizante',
      imagenUrl: '',
      tiempoPreparacion: 2,
      activo: true,
    };
    component.accionable = false;
    fixture.detectChanges();

    const fallback = fixture.nativeElement.querySelector('.producto-imagen--fallback');
    expect(fallback).toBeTruthy();
    expect(fallback.textContent).toContain('PRE ENTRENO');
    expect(fixture.nativeElement.querySelector('img.producto-imagen')).toBeNull();
  });

  it('vuelve al placeholder si la imagen falla', () => {
    component.bebida = {
      id: 3,
      nombre: 'Oreo',
      descripcion: 'Frappé',
      precio: 19,
      categoria: 'Clasico',
      imagenUrl: 'assets/bebidas/no-existe.jpg',
      tiempoPreparacion: 4,
      activo: true,
    };
    component.accionable = false;
    fixture.detectChanges();

    const img: HTMLImageElement = fixture.nativeElement.querySelector('img.producto-imagen');
    component.manejarErrorImagen({ currentTarget: img } as unknown as Event);

    expect(img.getAttribute('src')).toBe(component.imagenPredeterminada);
  });
});
