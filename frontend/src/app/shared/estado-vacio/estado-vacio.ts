import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Boton } from '../boton/boton';

/** Estado vacio uniforme para tablas y listas (tareas 3.13 y 4.8). */
@Component({
  selector: 'app-estado-vacio',
  standalone: true,
  imports: [CommonModule, Boton],
  templateUrl: './estado-vacio.html',
  styleUrl: './estado-vacio.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EstadoVacio {
  @Input() titulo = 'Sin resultados';
  @Input() mensaje = 'No hay datos para mostrar con los filtros actuales.';
  /** Emoji o icono textual; decorativo. */
  @Input() icono = '📋';
  @Input() accionTexto = '';

  @Output() accion = new EventEmitter<void>();
}
