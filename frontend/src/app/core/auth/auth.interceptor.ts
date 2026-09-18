import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { API_URL } from '../api';
import { AuthService } from './auth.service';

/** Añade el JWT a las llamadas a la API y expulsa al usuario si el backend responde 401. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const isApiCall = req.url.startsWith(API_URL);
  const isLogin = req.url === `${API_URL}/auth/login`;
  const token = auth.token();

  const authorized =
    isApiCall && !isLogin && token
      ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : req;

  return next(authorized).pipe(
    catchError((err: unknown) => {
      if (err instanceof HttpErrorResponse && err.status === 401 && isApiCall && !isLogin) {
        auth.logout();
      }
      return throwError(() => err);
    }),
  );
};
