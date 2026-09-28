import { Routes } from '@angular/router';
import { Login } from './pages/auth/login/login';
import { Home } from './pages/home/home';
import { Cuenta } from './pages/cuenta/cuenta';
import { Ventas } from './pages/ventas/ventas';
import { Empleados } from './pages/empleados/empleados';
import { authGuard, guestGuard } from './guard/auth-guard';
import { adminGuard, empleadoGuard } from './guard/role.guard';
import { Menu } from './pages/menu/menu';
import { Ordenes } from './pages/ordenes/ordenes';
import { Dashboard } from './pages/dashboard/dashboard';
import { Inventario } from './pages/admin/inventario/inventario';
import { Usuarios } from './pages/admin/usuarios/usuarios';
import { Reportes } from './pages/admin/reportes/reportes';

export const routes: Routes = [
  { path: '', component: Login, canActivate: [guestGuard] },
  { path: 'home', component: Home, canActivate: [authGuard] },
  { path: 'cuenta', component: Cuenta, canActivate: [authGuard] },
  { path: 'ventas', component: Ventas, canActivate: [authGuard] },
  { path: 'empleados', component: Empleados, canActivate: [authGuard] },
  // Modulo empleado
  { path: 'menu', component: Menu, canActivate: [empleadoGuard] },
  { path: 'ordenes', component: Ordenes, canActivate: [empleadoGuard] },
  // Modulo admin
  { path: 'dashboard', component: Dashboard, canActivate: [adminGuard] },
  { path: 'admin/inventario', component: Inventario, canActivate: [adminGuard] },
  { path: 'admin/usuarios', component: Usuarios, canActivate: [adminGuard] },
  { path: 'admin/reportes', component: Reportes, canActivate: [adminGuard] },
  { path: '**', redirectTo: '/home' },
];
