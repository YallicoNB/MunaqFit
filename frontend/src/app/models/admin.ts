export interface Usuario {
  id?: number;
  dni: string;
  nombreCompleto: string;
  email: string;
  password?: string;
  rol: 'ADMIN' | 'EMPLEADO';
  estado: 'ACTIVO' | 'INACTIVO';
  ultimoLogin: string | null;
  fechaCreacion: string;
}

/** Cuerpo que espera POST /api/admin/usuarios (siempre crea como EMPLEADO). */
export interface CrearUsuarioRequest {
  dni: string;
  nombreCompleto: string;
  email: string;
  password: string;
}

export interface ProductoInventario {
  id: number;
  nombre: string;
  stockActual: number;
  stockMinimo: number;
  stockCritico: number;
  unidadMedida: string;
  costoUnitario: number;
  fechaCaducidad: string | null;
}

export interface ReabastecerRequest {
  productoId: number;
  cantidad: number;
  motivo?: string;
  comprobante?: string;
  proveedorId?: number;
  fechaCaducidad?: string;
}

export interface Proveedor {
  id: number;
  nombre: string;
  ruc: string | null;
  telefono: string | null;
  direccion: string | null;
  contactoNombre: string | null;
  estado: 'ACTIVO' | 'INACTIVO';
  tipoContrato: 'FIJO' | 'VARIABLE' | null;
}

export interface DashboardMetrics {
  ventasHoy: number;
  totalClientes: number;
  productosActivos: number;
  alertasStockCritico: number;
}

export interface ReporteVentas {
  totalIngresos: number;
  totalTransacciones: number;
  promedioDiario: number;
  productoMasVendido: string;
  ingresosPorMetodoPago: Record<string, number>;
}

export interface RankingBebida {
  posicion: number;
  bebidaId: number;
  nombreBebida: string;
  cantidadVendida: number;
  totalRecaudado: number;
  porcentajeTotal: number;
}
