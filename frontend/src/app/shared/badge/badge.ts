import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

/** Etiqueta de estado reutilizable (ACTIVO, CRITICO, PAGADO, etc.). */
@Component({
  selector: 'app-badge',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './badge.html',
  styleUrl: './badge.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Badge {
  /** success | danger | warning | info | neutral */
  @Input() tono: 'success' | 'danger' | 'warning' | 'info' | 'neutral' = 'neutral';
  /** Pinta un punto de color antes del texto. */
  @Input() punto = false;
  /** Texto alternativo cuando se usa solo con <ng-content>. */
  @Input() texto = '';
}
