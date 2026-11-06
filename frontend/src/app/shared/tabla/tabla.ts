import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Spinner } from '../spinner/spinner';
import { EstadoVacio } from '../estado-vacio/estado-vacio';
import { Boton } from '../boton/boton';

export interface ColumnaTabla {
  /** Propiedad de la fila que se pinta por defecto. */
  clave: string;
  titulo: string;
  alineacion?: 'izquierda' | 'centro' | 'derecha';
  ancho?: string;
  /** Texto calculado; tiene prioridad sobre fila[clave]. */
  valor?: (fila: any) => string;
  /** Clase CSS extra para la celda (por ejemplo, un estado). */
  clase?: (fila: any) => string;
}

/**
 * Tabla reutilizable con estados de carga, error y vacio incorporados
 * (tareas 3.4 y 4.8). La usan el kardex, usuarios, inventario y ordenes.
 */
@Component({
  selector: 'app-tabla',
  standalone: true,
  imports: [CommonModule, Spinner, EstadoVacio, Boton],
  templateUrl: './tabla.html',
  styleUrl: './tabla.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Tabla {
  @Input() columnas: ColumnaTabla[] = [];
  @Input() filas: any[] = [];
  @Input() cargando = false;
  @Input() error = '';
  @Input() tituloVacio = 'Sin resultados';
  @Input() mensajeVacio = 'No hay datos para mostrar con los filtros actuales.';
  @Input() ariaLabel = 'Tabla de datos';
  @Input() clickeable = false;
  @Input() mostrarAcciones = false;
  @Input() textoAccion = 'Acción';

  @Output() filaClick = new EventEmitter<any>();
  @Output() accion = new EventEmitter<any>();

  valorCelda(fila: any, columna: ColumnaTabla): string {
    if (columna.valor) {
      return columna.valor(fila);
    }
    const valor = fila?.[columna.clave];
    return valor === null || valor === undefined || valor === '' ? '—' : String(valor);
  }

  claseCelda(fila: any, columna: ColumnaTabla): string {
    return columna.clase ? columna.clase(fila) : '';
  }

  alineacion(columna: ColumnaTabla): string {
    switch (columna.alineacion) {
      case 'centro':
        return 'center';
      case 'derecha':
        return 'right';
      default:
        return 'left';
    }
  }

  alClick(fila: any): void {
    if (this.clickeable) {
      this.filaClick.emit(fila);
    }
  }

  trackFila(_indice: number, fila: any): unknown {
    return fila?.id ?? fila;
  }
}
