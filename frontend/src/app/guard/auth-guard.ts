import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Token } from '../service/auth/token';

/**
 * Resumen de acceso por rol:
 * - ADMIN    -> home, cuenta, ventas, empleados, dashboard, admin/inventario, admin/usuarios, admin/reportes
 * - EMPLEADO -> home, cuenta, ventas, menu, ordenes
 * - Sin sesion -> solo login ("")
 */

/** Valida la sesion una sola vez y devuelve si es valida junto con el rol actual. */
function estadoSesion(): { valida: boolean; rol: string | null } {
  const token = inject(Token);
  const payload = token.decodificarToken();
  if (!payload) {
    return { valida: false, rol: null };
  }
  return {
    valida: true,
    rol: payload.role ?? payload.rol ?? localStorage.getItem('rolUsuario'),
  };
}

/**
 * Administrador y empleado (cualquier usuario con sesion).
 * Paginas permitidas: home, cuenta.
 */
export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);

  console.log('authGuard | ruta evaluada:', route.routeConfig?.path);
  console.log('authGuard | URL destino:', state.url);

  if (estadoSesion().valida) {
    return true;
  }

  return router.createUrlTree(['/']);
};

/**
 * Visitante sin sesion. Paginas permitidas: login ("").
 * Si ya esta logueado lo manda a /home.
 */
export const guestGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);

  console.log('guestGuard | ruta evaluada:', route.routeConfig?.path);
  console.log('guestGuard | URL destino:', state.url);

  if (estadoSesion().valida) {
    return router.createUrlTree(['/home']);
  }

  return true;
};

/**
 * Deja pasar solo a los usuarios con uno de los roles indicados.
 * Se usa en las rutas de admin y de empleado.
 */
export function roleGuard(roles: string[]): CanActivateFn {
  return () => {
    const router = inject(Router);
    const sesion = estadoSesion();

    if (!sesion.valida) {
      return router.createUrlTree(['/']);
    }

    if (sesion.rol && roles.includes(sesion.rol)) {
      return true;
    }

    // Si no tiene permiso, vuelve a su pagina de inicio
    return router.createUrlTree(['/home']);
  };
}

/**
 * SOLO ADMIN.
 * Paginas permitidas: dashboard, admin/inventario, admin/usuarios, admin/reportes, empleados.
 */
export const adminGuard: CanActivateFn = roleGuard(['ADMIN']);

/**
 * SOLO EMPLEADO.
 * Paginas permitidas: menu, ordenes.
 */
export const empleadoGuard: CanActivateFn = roleGuard(['EMPLEADO']);

/**
 * ADMIN y EMPLEADO.
 * Paginas permitidas: ventas (punto de venta).
 */
export const personalGuard: CanActivateFn = roleGuard(['ADMIN', 'EMPLEADO']);