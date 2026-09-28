/** Tipos de notificacion que muestra el toast. */
export type ToastType = 'success' | 'danger' | 'warning' | 'info';

export interface Toast {
  id: number;
  tipo: ToastType;
  title: string;
  message: string;
}