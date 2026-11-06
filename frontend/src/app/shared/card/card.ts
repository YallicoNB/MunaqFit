import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

/**
 * Tarjeta de contenido con cabecera opcional y una zona de acciones
 * proyectada con el atributo `acciones`.
 *
 * Ejemplo:
 * <app-card titulo="Inventario" subtitulo="Stock actual">
 *   <app-boton acciones>Reabastecer</app-boton>
 *   ...contenido...
 * </app-card>
 */
@Component({
  selector: 'app-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './card.html',
  styleUrl: './card.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Card {
  @Input() titulo = '';
  @Input() subtitulo = '';
  /** Añade la clase mq-card de los tokens globales (fondo, sombra, radio). */
  @Input() conEstilo = true;
}
