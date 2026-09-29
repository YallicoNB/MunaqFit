import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CardHomeMenu } from './card-home-menu';

describe('CardHomeMenu', () => {
  let component: CardHomeMenu;
  let fixture: ComponentFixture<CardHomeMenu>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CardHomeMenu]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CardHomeMenu);
    component = fixture.componentInstance;
    component.bebida = {
      id: 1,
      nombre: 'Bebida de prueba',
      descripcion: 'Descripción de prueba',
      precio: 12,
      categoria: 'Frías',
      imagenUrl: '',
      tiempoPreparacion: 0,
      activo: true,
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
