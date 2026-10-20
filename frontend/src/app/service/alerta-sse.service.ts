import { Injectable, NgZone } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs';
import { API_URL } from '../core/api';
import { Token } from './auth/token';
import { ToastService } from '../shared/toast/toast.service';

/** Payload del evento SSE de stock. */
interface AlertaStock {
  productoId: number;
  nombre: string;
  stockActual: number;
  stockCritico: number;
  unidad: string;
  tipo: 'CRITICO' | 'RECUPERADO';
  mensaje: string;
}

/**
 * Alertas de stock en tiempo real (REQ-021 / REQ-031, reparto 3 - Persona 1).
 *
 * Se suscribe al stream SSE del backend con EventSource. Como EventSource no
 * puede mandar headers, el JWT viaja en ?token= (el backend lo valida).
 * La suscripcion se (re)intenta en cada navegacion: asi queda conectada
 * apenas el empleado inicia sesion, sin recargar la pagina.
 */
@Injectable({ providedIn: 'root' })
export class AlertaSseService {
  private fuente: EventSource | null = null;
  private reintento: ReturnType<typeof setTimeout> | null = null;

  constructor(
    private token: Token,
    private toast: ToastService,
    private router: Router,
    private zona: NgZone
  ) {
    this.router.events
      .pipe(filter((evento) => evento instanceof NavigationEnd))
      .subscribe(() => this.conectarSiAutenticado());
  }

  /** Abre el stream solo si hay sesion valida y aun no esta conectado. */
  conectarSiAutenticado(): void {
    if (this.fuente || !this.token.decodificarToken()) {
      return;
    }
    const jwt = this.token.getToken();
    if (!jwt) {
      return;
    }

    const url = `${API_URL}/alertas/stock?token=${encodeURIComponent(jwt)}`;
    const fuente = new EventSource(url);

    fuente.addEventListener('stock-critico', (evento) =>
      this.zona.run(() => this.alertarCritico(evento as MessageEvent<string>))
    );
    fuente.addEventListener('stock-recuperado', (evento) =>
      this.zona.run(() => this.alertarRecuperado(evento as MessageEvent<string>))
    );
    // Si el stream muere (corte de red, token vencido), se cierra y se
    // reintenta en unos segundos mientras haya sesion.
    fuente.onerror = () => this.zona.run(() => this.reconectar(fuente));

    this.fuente = fuente;
  }

  /** Cierra el stream (se usa al cerrar sesion). */
  desconectar(): void {
    if (this.reintento) {
      clearTimeout(this.reintento);
      this.reintento = null;
    }
    this.fuente?.close();
    this.fuente = null;
  }

  // ===================== Internos =====================

  private alertarCritico(evento: MessageEvent<string>): void {
    const alerta = this.parsear(evento.data);
    if (alerta) {
      this.toast.warning(alerta.mensaje, 'Stock critico');
    }
  }

  private alertarRecuperado(evento: MessageEvent<string>): void {
    const alerta = this.parsear(evento.data);
    if (alerta) {
      this.toast.success(alerta.mensaje, 'Stock recuperado');
    }
  }

  private reconectar(fuente: EventSource): void {
    fuente.close();
    if (this.fuente === fuente) {
      this.fuente = null;
    }
    if (!this.token.decodificarToken() || this.reintento) {
      return;
    }
    this.reintento = setTimeout(() => {
      this.reintento = null;
      this.conectarSiAutenticado();
    }, 8000);
  }

  private parsear(json: string): AlertaStock | null {
    try {
      return JSON.parse(json) as AlertaStock;
    } catch {
      return null;
    }
  }
}
