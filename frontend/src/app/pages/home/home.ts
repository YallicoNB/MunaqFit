import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { Navbar } from '../../layouts/navbar/navbar';
import { Token } from '../../service/auth/token';

@Component({
  selector: 'app-home',
  imports: [Navbar],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {
  constructor(private token: Token, private router: Router) {}

  eliminarToken() {
    this.token.cerrarSesion();
    this.router.navigate(['/']);
  }
}