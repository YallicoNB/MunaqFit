import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterOutlet } from '@angular/router';
import { ToastContainer } from './shared/toast/toast-container.component';
import { Navbar } from './layouts/navbar/navbar';

@Component({
  selector: 'app-root',
  imports: [CommonModule, RouterOutlet, ToastContainer, Navbar],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  cuentaAbierta = false;

  constructor(private router: Router) {}

  get mostrarNavegacion(): boolean {
    return this.router.url !== '' && this.router.url !== '/';
  }

  cambiarEstadoCuenta(abierto: boolean): void {
    this.cuentaAbierta = abierto;
  }
}