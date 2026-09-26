import { Component } from '@angular/core';
import { RegisterComponent } from './pages/auth/register/register';

@Component({
  selector: 'app-root',
  imports: [RegisterComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {}
