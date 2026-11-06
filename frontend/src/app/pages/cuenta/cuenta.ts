import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Navbar } from '../../layouts/navbar/navbar';
import { Token } from '../../service/auth/token';

@Component({
  selector: 'app-cuenta',
  imports: [Navbar],
  templateUrl: './cuenta.html',
  styleUrl: './cuenta.css',
})
export class Cuenta implements OnInit {
  dni = '';
  nombre = '';
  email = '';
  rol = '';
  rolLegible = '';

  constructor(private token: Token, private router: Router) {}

  ngOnInit(): void {
    this.dni = this.token.getDni() ?? 'No disponible';
    this.nombre = this.token.getNombre() ?? 'No disponible';
    this.email = this.token.getEmail() ?? 'No disponible';
    this.rol = this.token.getRol() ?? 'No disponible';
    this.rolLegible = this.rol === 'ADMIN' ? 'Administrador' : 'Empleado';
  }

  cerrarSesion(): void {
    this.token.cerrarSesion();
    this.router.navigate(['/']);
  }
}
