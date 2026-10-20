import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterOutlet } from '@angular/router';
import { ToastContainer } from './shared/toast/toast-container.component';
import { Navbar } from './layouts/navbar/navbar';
import { AlertaSseService } from './service/alerta-sse.service';

@Component({
  selector: 'app-root',
  imports: [CommonModule, RouterOutlet, ToastContainer, Navbar],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  cuentaAbierta = false;

  constructor(
    private router: Router,
    // Alertas de stock en tiempo real (REQ-021): el servicio se suscribe
    // solo cuando hay sesion y reconecta en cada navegacion.
    private alertasStock: AlertaSseService
  ) {}

  get mostrarNavegacion(): boolean {
    return this.router.url !== '' && this.router.url !== '/';
  }

  cambiarEstadoCuenta(abierto: boolean): void {
    this.cuentaAbierta = abierto;
  }
}