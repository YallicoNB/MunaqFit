import { Injectable, signal } from '@angular/core';
import { Toast, ToastType } from './toast.model';

/**
 * Servicio global de notificaciones (toasts).
 * Cualquier componente lo inyecta y llama a success/error/warning/info;
 * el contenedor (<app-toast-container>) ya esta montado en <app-root>.
 */
@Injectable({ providedIn: 'root' })
export class ToastService {
  private readonly lista = signal<Toast[]>([]);
  /** Toasts visibles. En la plantilla se lee como propiedad: toasts() */
  readonly toasts = this.lista.asReadonly();

  success(message: string, title = 'Correcto'): void {
    this.agregar('success', title, message);
  }

  error(message: string, title = 'Error'): void {
    this.agregar('danger', title, message);
  }

  warning(message: string, title = 'Atencion'): void {
    this.agregar('warning', title, message);
  }

  info(message: string, title = 'Informacion'): void {
    this.agregar('info', title, message);
  }

  /** Quita un toast; lo usan el boton de cerrar y el temporizador. */
  eliminar(id: number): void {
    this.lista.update((actuales) => actuales.filter((t) => t.id !== id));
  }

  /** Descarta todos los toasts pendientes. */
  limpiar(): void {
    this.lista.set([]);
  }

  private agregar(tipo: ToastType, title: string, message: string): void {
    const toast: Toast = { id: Date.now() + Math.random(), tipo, title, message };
    this.lista.update((actuales) => [...actuales, toast]);
    // Se oculta solo a los 4 segundos; el temporizador no bloquea la UI.
    setTimeout(() => this.eliminar(toast.id), 4000);
  }
}