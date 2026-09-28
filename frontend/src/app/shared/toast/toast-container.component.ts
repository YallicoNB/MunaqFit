import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from './toast.service';

/**
 * Contenedor fijo que muestra los toasts activos del ToastService.
 * Se monta una sola vez en <app-root>; ninguna pagina lo importa.
 */
@Component({
  selector: 'app-toast-container',
  imports: [CommonModule],
  templateUrl: './toast-container.html',
  styleUrl: './toast-container.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ToastContainer {
  private toastService = inject(ToastService);
  readonly toasts = this.toastService.toasts;

  /** Clase de color de Bootstrap segun el tipo de toast. */
  tipoClase(tipo: string): string {
    switch (tipo) {
      case 'success':
        return 'text-bg-success';
      case 'danger':
        return 'text-bg-danger';
      case 'warning':
        return 'text-bg-warning';
      default:
        return 'text-bg-info';
    }
  }

  cerrar(id: number): void {
    this.toastService.eliminar(id);
  }
}