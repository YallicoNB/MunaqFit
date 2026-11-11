export interface RespuestaLogin {
  token: string;
  refreshToken: string;
  tipo: string;
  idUsuario: number;
  dni: string;
  nombreCompleto: string;
  email: string;
  rol: string;
  expiraEn: number;
}
