import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

/** Indicador de carga accesible (role="status" + texto para lectores). */
@Component({
  selector: 'app-spinner',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './spinner.html',
  styleUrl: './spinner.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Spinner {
  /** sm | md | lg */
  @Input() tamano: 'sm' | 'md' | 'lg' = 'md';
  /** Texto que anuncia el lector de pantalla. */
  @Input() etiqueta = 'Cargando…';
  /** Centra el indicador en su contenedor. */
  @Input() centrado = false;
}
