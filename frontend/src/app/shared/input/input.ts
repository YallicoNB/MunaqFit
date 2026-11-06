import {
  Component,
  Input,
  Optional,
  Self,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NgControl } from '@angular/forms';

let contadorInputs = 0;

/**
 * Input de formulario accesible y reutilizable (tareas 3.4 y 4.4/4.5).
 *
 * - `<label>` real asociado por id.
 * - `aria-invalid` / `aria-describedby` automaticos.
 * - Funciona con `formControlName` y con `[(ngModel)]` (ControlValueAccessor).
 * - Si no se pasa `error`, muestra los errores de validacion de Angular en
 *   espanol cuando el control fue tocado.
 */
@Component({
  selector: 'app-input',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './input.html',
  styleUrl: './input.css',
})
export class CampoInput implements ControlValueAccessor {
  @Input() label = '';
  @Input() tipo: 'text' | 'email' | 'password' | 'number' | 'date' | 'search' | 'tel' = 'text';
  @Input() id = `mq-input-${++contadorInputs}`;
  @Input() placeholder = '';
  @Input() autocomplete = 'off';
  @Input() ayuda = '';
  /** Error explicito. Si esta vacio se derivan los errores del formulario. */
  @Input() error = '';
  @Input() requerido = false;
  @Input() deshabilitado = false;
  @Input() min: string | number | null = null;
  @Input() max: string | number | null = null;
  @Input() step: string | number | null = null;
  /** Texto opcional a la derecha (por ejemplo, un sufijo de unidad). */
  @Input() sufijo = '';

  valor = '';
  private tocado = false;
  private onChange: (valor: string) => void = () => undefined;
  private onTouched: () => void = () => undefined;

  constructor(@Self() @Optional() public ngControl: NgControl | null) {
    if (this.ngControl) {
      this.ngControl.valueAccessor = this;
    }
  }

  get idAyuda(): string {
    return `${this.id}-ayuda`;
  }

  get idError(): string {
    return `${this.id}-error`;
  }

  get descritoPor(): string | null {
    if (this.mostrarError()) return this.idError;
    return this.ayuda ? this.idAyuda : null;
  }

  mostrarError(): string {
    if (this.error) return this.error;
    if (!this.tocado && !this.ngControl?.touched) return '';

    const control = this.ngControl?.control;
    if (!control || control.valid) return '';

    if (control.hasError('required')) return 'Este campo es obligatorio';
    if (control.hasError('email')) return 'Ingresa un correo válido';
    if (control.hasError('minlength')) {
      return `Mínimo ${control.getError('minlength').requiredLength} caracteres`;
    }
    if (control.hasError('maxlength')) {
      return `Máximo ${control.getError('maxlength').requiredLength} caracteres`;
    }
    if (control.hasError('pattern')) return 'El formato no es válido';
    if (control.hasError('min')) return `El valor mínimo es ${control.getError('min').min}`;
    if (control.hasError('max')) return `El valor máximo es ${control.getError('max').max}`;
    return 'El valor no es válido';
  }

  alCambiar(evento: Event): void {
    const nuevo = (evento.target as HTMLInputElement).value;
    this.valor = nuevo;
    this.onChange(nuevo);
  }

  alSalir(): void {
    this.tocado = true;
    this.onTouched();
  }

  writeValue(valor: unknown): void {
    this.valor = valor === null || valor === undefined ? '' : String(valor);
  }

  registerOnChange(fn: (valor: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(deshabilitado: boolean): void {
    this.deshabilitado = deshabilitado;
  }
}
