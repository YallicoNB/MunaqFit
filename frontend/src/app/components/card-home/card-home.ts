import { Component, Input } from '@angular/core';

export interface Promocion {
  titulo: string;
  descripcion: string;
  imagen: string;
  textoAlternativo: string;
}

@Component({
  selector: 'app-card-home',
  imports: [],
  templateUrl: './card-home.html',
  styleUrl: './card-home.css',
})
export class CardHome {
  @Input({ required: true }) promocion!: Promocion;
}
