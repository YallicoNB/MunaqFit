// Entorno de produccion (Vercel).
// Backend desplegado en Render. El nombre del servicio esta definido en
// render.yaml (munaqfit-backend). Si cambias el nombre o el dominio,
// actualiza esta URL y APP_CORS_ALLOWED_ORIGINS en Render.
export const environment = {
  production: true,
  apiUrl: 'https://munaqfit-backend.onrender.com/api',
};
