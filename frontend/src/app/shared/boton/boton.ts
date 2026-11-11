import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Spinner } from '../spinner/spinner';

/**
 * Boton unico para toda la app (tarea 3.4 de la etapa 2).
 *
 * Mantiene el click nativo (para formularios y atajos) y ademas emite
 * `(accion)`. El estado `cargando` deshabilita el boton y muestra el spinner,
 * evitando el doble envio de formularios.
 */
@Component({
  selector: 'app-boton',
  standalone: true,
  imports: [CommonModule, Spinner],
  templateUrl: './boton.html',
  styleUrl: './boton.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Boton {
  /** primary | secondary | danger | ghost */
  @Input() tipo: 'primary' | 'secondary' | 'danger' | 'ghost' = 'primary';
  /** sm | md | lg */
  @Input() tamano: 'sm' | 'md' | 'lg' = 'md';
  @Input() type: 'button' | 'submit' | 'reset' = 'button';
  @Input() deshabilitado = false;
  @Input() cargando = false;
  @Input() bloque = false;
  /** Obligatorio cuando el boton solo lleva un icono. */
  @Input() ariaLabel = '';

  @Output() accion = new EventEmitter<MouseEvent>();

  alPresionar(evento: MouseEvent): void {
    if (this.deshabilitado || this.cargando) {
      evento.preventDefault();
      evento.stopPropagation();
      return;
    }
    this.accion.emit(evento);
  }
}
