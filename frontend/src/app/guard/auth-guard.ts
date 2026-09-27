import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Token } from '../service/auth/token';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const token = inject(Token);

  console.log('authGuard | ruta evaluada:', route.routeConfig?.path);
  console.log('authGuard | URL destino:', state.url);

  if (token.decodificarToken()) {
    return true;
  }

  return router.createUrlTree(['/']);
};

export const guestGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const token = inject(Token);

  console.log('guestGuard | ruta evaluada:', route.routeConfig?.path);
  console.log('guestGuard | URL destino:', state.url);

  if (token.decodificarToken()) {
    return router.createUrlTree(['/home']);
  }

  return true;
};