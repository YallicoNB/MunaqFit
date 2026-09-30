import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NavbarCuenta } from './navbar-cuenta';

describe('NavbarCuenta', () => {
  let component: NavbarCuenta;
  let fixture: ComponentFixture<NavbarCuenta>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavbarCuenta]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NavbarCuenta);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
