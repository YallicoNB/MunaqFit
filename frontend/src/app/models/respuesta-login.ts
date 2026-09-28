export interface RespuestaLogin {
  token: string;
  tokenType: string;
  id: number;
  dni: string;
  nombreCompleto: string;
  email: string;
  rol: string;
  expirationMs: number;
}