export interface DetalleVenta {
  bebida: { id: number };
  cantidad: number;
  precioUnitario?: number;
  subtotal?: number;
}

export interface VentaRequest {
  venta: {
    mesa?: number;
    tipoVenta?: 'LOCAL' | 'DELIVERY';
    notas?: string;
  };
  detalles: DetalleVenta[];
}

export interface Venta {
  id: number;
  numeroPedido: string;
  mesa: number | null;
  tipoVenta: 'LOCAL' | 'DELIVERY';
  subtotal: number;
  igv: number;
  total: number;
  estado: 'PENDIENTE' | 'PAGADO' | 'CANCELADO';
  notas: string | null;
  fechaHora: string;
  /** El backend siempre lo envia (usuario_id es NOT NULL), pero se tipa opcional por si el token caduca. */
  usuario: { id: number; nombreCompleto: string } | null;
}

export interface DetalleVentaRespuesta extends DetalleVenta {
  id: number;
  subtotal: number;
  bebida: { id: number; nombre: string; precio: number };
}

export interface PagoRequest {
  ventaId: number;
  montoPagado: number;
  tipoPago?: 'EFECTIVO' | 'YAPE' | 'PLIN' | 'TRANSFERENCIA' | 'QR';
  numeroOperacion?: string;
  banco?: string;
}

export interface PagoRespuesta {
  pagoId: number;
  ventaId: number;
  numeroPedido: string;
  montoTotal: number;
  montoPagado: number;
  vuelto: number;
  tipoPago: string;
  numeroOperacion: string | null;
  banco: string | null;
  estadoPago: 'PENDIENTE' | 'COMPLETADO';
  estadoVenta: 'PENDIENTE' | 'PAGADO' | 'CANCELADO';
  fechaPago: string;
}

export interface ClienteFidelidad {
  id: number;
  nombre: string;
  telefono: string;
  email: string | null;
  visitas: number;
  umbralPremio: number;
  ultimaVisita: string | null;
  fechaRegistro: string;
}
