import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Usuario } from '../../../models/usuario';

@Component({
  selector: 'app-register',
  imports: [FormsModule],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class RegisterComponent {
  //iniciamos variables para el registro
  dni: string = '';
  password: string = '';

  validarDni(): boolean {
    return /^\d{8}$/.test(this.dni);
  }

  validarPassword(): boolean {
    return this.password.length > 8;
  }

  registrar() {
    if (!this.validarDni()) {
      console.log('Error: el DNI debe tener 8 dígitos');
      return;
    }

    if (!this.validarPassword()) {
      console.log('Error: el password debe tener más de 8 caracteres');
      return;
    }

    const usuario = new Usuario(this.dni, this.password);
    // Si todo es válido, imprimir los datos
    console.log('Registro exitoso');
    console.log('DNI:', this.dni);
    console.log('Password:', this.password);

    //formato del usuario
    console.log(usuario);

    this.dni = '';
    this.password = '';
  }
}