import { Routes } from '@angular/router';
import { Login } from './pages/auth/login/login';
import { Home } from './pages/home/home';
import { Cuenta } from './pages/cuenta/cuenta';
import { Ventas } from './pages/ventas/ventas';
import { Empleados } from './pages/empleados/empleados';
import { authGuard, guestGuard, adminGuard, empleadoGuard, personalGuard } from './guard/auth-guard';
import { Menu } from './pages/menu/menu';
import { Ordenes } from './pages/ordenes/ordenes';
import { Dashboard } from './pages/dashboard/dashboard';
import { Inventario } from './pages/admin/inventario/inventario';
import { Usuarios } from './pages/admin/usuarios/usuarios';
import { Reportes } from './pages/admin/reportes/reportes';
import { KardexComponent } from './pages/admin/kardex/kardex.component';

export const routes: Routes = [
  // Sin sesion: solo el login
  { path: '', component: Login, canActivate: [guestGuard] },

  // ADMIN y EMPLEADO: paginas comunes
  { path: 'home', component: Home, canActivate: [authGuard] },
  { path: 'cuenta', component: Cuenta, canActivate: [authGuard] },
  { path: 'ventas', component: Ventas, canActivate: [personalGuard] },

  // SOLO ADMIN
  { path: 'empleados', component: Empleados, canActivate: [adminGuard] },
  { path: 'dashboard', component: Dashboard, canActivate: [adminGuard] },
  { path: 'admin/inventario', component: Inventario, canActivate: [adminGuard] },
  { path: 'admin/usuarios', component: Usuarios, canActivate: [adminGuard] },
  { path: 'admin/reportes', component: Reportes, canActivate: [adminGuard] },

  // SOLO EMPLEADO
  { path: 'menu', component: Menu, canActivate: [empleadoGuard] },
  { path: 'ordenes', component: Ordenes, canActivate: [empleadoGuard] },

  { path: 'admin/kardex', component: KardexComponent, canActivate: [adminGuard] },
  { path: '**', redirectTo: '/home' },
];
