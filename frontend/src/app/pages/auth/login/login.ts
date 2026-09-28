import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Usuario } from '../../../models/usuario';
import { Login as LoginService } from '../../../service/auth/login';
import { Token } from '../../../service/auth/token';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  dni: string = '';
  password: string = '';

  constructor(
    private loginService: LoginService,
    private token: Token,
    private router: Router,
  ) {}

  iniciarSesion() {
    const usuario = new Usuario(this.dni, this.password);
    console.log('DNI:', this.dni);
    console.log('Password:', this.password);
    console.log(usuario);

    this.loginService.iniciarSesion(this.dni, this.password).subscribe({
      next: (respuesta) => {
        console.log('Respuesta:', respuesta);

        this.token.guardarSesion(respuesta);

        this.router.navigate(['/home']);
      },
      error: (error) => console.error('Error:', error),
    });
  }
}
