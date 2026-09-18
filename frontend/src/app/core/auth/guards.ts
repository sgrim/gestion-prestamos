import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { Role } from '../models';
import { AuthService } from './auth.service';

/*
 * Los guards mejoran la experiencia (no muestran pantallas a las que no se tiene acceso), pero NO
 * son la barrera de seguridad: el backend valida el JWT y el rol en cada petición.
 */

/** Exige sesión válida; si no, lleva al login. */
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.hasValidSession() || inject(Router).createUrlTree(['/login']);
};

/** Solo para visitantes: si ya hay sesión, redirige a la pantalla de su rol. */
export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.hasValidSession() ? inject(Router).createUrlTree([auth.homeUrl()]) : true;
};

/** Exige un rol concreto; si el usuario tiene otro, lo devuelve a su pantalla. */
export const roleGuard =
  (role: Role): CanActivateFn =>
  () => {
    const auth = inject(AuthService);
    return auth.user()?.role === role || inject(Router).createUrlTree([auth.homeUrl()]);
  };

/** Ruta raíz: decide a dónde enviar al usuario. */
export const homeRedirectGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const target = auth.hasValidSession() ? auth.homeUrl() : '/login';
  return inject(Router).createUrlTree([target]);
};
