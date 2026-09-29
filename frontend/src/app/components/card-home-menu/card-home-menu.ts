import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { Bebida } from '../../models/producto';

@Component({
  selector: 'app-card-home-menu',
  imports: [CommonModule],
  templateUrl: './card-home-menu.html',
  styleUrl: './card-home-menu.css',
})
export class CardHomeMenu {
  @Input({ required: true }) bebida!: Bebida;

  readonly imagenPredeterminada = '/assets/bebidas frias.jpg';

  get imagen(): string {
    return this.bebida.imagenUrl?.trim() || this.imagenPredeterminada;
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
