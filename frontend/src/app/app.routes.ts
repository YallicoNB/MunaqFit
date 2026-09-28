import { Routes } from '@angular/router';
import { Login } from './pages/auth/login/login';
import { RegisterComponent } from './pages/auth/register/register';
import { Home } from './pages/home/home';
import { Cuenta } from './pages/cuenta/cuenta';
import { Ventas } from './pages/ventas/ventas';
import { Empleados } from './pages/empleados/empleados';
import { authGuard, guestGuard } from './guard/auth-guard';

export const routes: Routes = [
  { path: '', component: Login, canActivate: [guestGuard] },
  { path: 'register', component: RegisterComponent, canActivate: [guestGuard] },
  { path: 'home', component: Home, canActivate: [authGuard] },
  { path: 'cuenta', component: Cuenta, canActivate: [authGuard] },
  { path: 'ventas', component: Ventas, canActivate: [authGuard] },
  { path: 'empleados', component: Empleados, canActivate: [authGuard] },
  { path: '**', redirectTo: '/home' },
];