import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FooterCuenta } from './footer-cuenta';

describe('FooterCuenta', () => {
  let component: FooterCuenta;
  let fixture: ComponentFixture<FooterCuenta>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FooterCuenta]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FooterCuenta);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
