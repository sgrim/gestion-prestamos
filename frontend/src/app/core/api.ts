import { HttpErrorResponse } from '@angular/common/http';

/** Ruta relativa: en Docker la resuelve nginx y en desarrollo el proxy de `ng serve` (proxy.conf.json). */
export const API_URL = '/api';

/** Convierte una respuesta de error del backend (ProblemDetail) en un mensaje legible. */
export function errorMessage(err: unknown, fallback = 'Ocurrió un error inesperado'): string {
  if (!(err instanceof HttpErrorResponse)) {
    return fallback;
  }
  if (err.status === 0) {
    return 'No se pudo conectar con el servidor';
  }
  const fieldErrors = err.error?.errors as Record<string, string> | undefined;
  if (fieldErrors) {
    return Object.values(fieldErrors).join('. ');
  }
  return err.error?.detail ?? fallback;
}
