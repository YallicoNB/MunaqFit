export interface Producto {
  id: number;
  nombre: string;
  categoria: { id: number; nombre: string } | null;
  proveedores: Array<{
    id: number;
    nombre: string;
    precioUnitario: number;
    esPrincipal: boolean;
  }>;
  stockActual: number;
  stockMinimo: number;
  stockCritico: number;
  unidadMedida: 'KG' | 'G' | 'L' | 'ML' | 'UNIDAD';
  costoUnitario: number;
  fechaCaducidad: string | null;
  ultimaReposicion: string | null;
}

export interface Receta {
  id: number;
  producto: Producto;
  cantidad: number;
  pasoInstruccion: string;
}

export interface Bebida {
  id: number;
  nombre: string;
  descripcion: string;
  precio: number;
  categoria: string;
  imagenUrl: string;
  tiempoPreparacion: number;
  activo: boolean;
  // Nuevos campos opcionales (REQ-018)
  calorias?: number;
  proteinas?: number;
  carbohidratos?: number;
  grasas?: number;
  fibra?: number;
  azucares?: number;
}
