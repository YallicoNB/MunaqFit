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

  readonly imagenPredeterminada = '/assets/2339b0e4bc1c48d152945529d0b168fa.jpg';
}
