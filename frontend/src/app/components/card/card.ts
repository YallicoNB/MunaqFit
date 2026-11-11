import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Bebida } from '../../models/producto';

@Component({
  selector: 'app-card',
  imports: [CommonModule],
  templateUrl: './card.html',
  styleUrl: './card.css',
})
export class Card {
  @Input({ required: true }) bebida!: Bebida;
  @Input() accionable = true;
  @Output() seleccionar = new EventEmitter<Bebida>();

  /** Imagen de respaldo si una URL viene rota (por ejemplo, un archivo movido). */
  readonly imagenPredeterminada = '/assets/2339b0e4bc1c48d152945529d0b168fa.jpg';

  get imagen(): string {
    return this.bebida.imagenUrl?.trim() || '';
  }

  manejarErrorImagen(evento: Event): void {
    const imagen = evento.currentTarget;
    if (
      imagen instanceof HTMLImageElement &&
      imagen.getAttribute('src') !== this.imagenPredeterminada
    ) {
      imagen.src = this.imagenPredeterminada;
    }
  }
}
