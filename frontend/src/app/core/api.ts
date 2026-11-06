import { environment } from '../../environments/environment';

/**
 * URL base del backend. Se centraliza aqui para que los servicios nuevos
 * no repitan la direccion.
 *
 * En desarrollo apunta a http://localhost:8080/api.
 * En produccion (Vercel) apunta al backend desplegado en Render.
 */
export const API_URL = environment.apiUrl;
