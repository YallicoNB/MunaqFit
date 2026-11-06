import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  HostListener,
  Input,
  Output,
} from '@angular/core';
import { CommonModule } from '@angular/common';

/**
 * Modal accesible y reutilizable (tarea 3.4).
 *
 * - `role="dialog"` + `aria-modal="true"` + titulo asociado.
 * - Cierra con la X, con Escape o con clic en el fondo.
 * - El contenido se proyecta; los botones de accion van con el atributo
 *   `acciones` para que aparezcan en el pie.
 */
@Component({
  selector: 'app-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './modal.html',
  styleUrl: './modal.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Modal {
  @Input() abierto = false;
  @Input() titulo = '';
  @Input() tamano: 'sm' | 'md' | 'lg' = 'md';
  @Input() cerrarConFondo = true;
  /** Oculta la X cuando el modal es una confirmacion obligatoria. */
  @Input() mostrarCerrar = true;

  @Output() cerrar = new EventEmitter<void>();

  @HostListener('document:keydown.escape')
  alEscape(): void {
    if (this.abierto && this.mostrarCerrar) {
      this.cerrar.emit();
    }
  }

  alClicFondo(evento: MouseEvent): void {
    if (this.cerrarConFondo && evento.target === evento.currentTarget) {
      this.cerrar.emit();
    }
  }

  cerrarModal(): void {
    this.cerrar.emit();
  }
}
