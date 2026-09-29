import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CardHome } from './card-home';

describe('CardHome', () => {
  let component: CardHome;
  let fixture: ComponentFixture<CardHome>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CardHome]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CardHome);
    component = fixture.componentInstance;
    component.promocion = {
      titulo: 'Promoción de prueba',
      descripcion: 'Descripción de prueba',
      imagen: 'assets/promocion_multicolor.jpg',
      textoAlternativo: 'Bebidas de colores',
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
