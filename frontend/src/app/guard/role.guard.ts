import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Token } from '../service/auth/token';

/**
 * Deja pasar solo a los usuarios con el rol indicado.
 * Se usa en las rutas de admin y de empleado.
 */
export function roleGuard(roles: string[]): CanActivateFn {
  return () => {
    const router = inject(Router);
    const token = inject(Token);

    const payload = token.decodificarToken();
    if (!payload) {
      return router.createUrlTree(['/']);
    }

    const rolActual = payload.role ?? payload.rol ?? localStorage.getItem('rolUsuario');

    if (rolActual && roles.includes(rolActual)) {
      return true;
    }

    // Si no tiene permiso, vuelve a su pagina de inicio
    return router.createUrlTree(['/home']);
  };
}

export const adminGuard: CanActivateFn = roleGuard(['ADMIN']);
export const empleadoGuard: CanActivateFn = roleGuard(['ADMIN', 'EMPLEADO']);
