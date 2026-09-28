export class Usuario {
  dni: string;
  password: string;

  constructor(dni: string, password: string) {
    this.dni = dni;
    this.password = password;
  }
}