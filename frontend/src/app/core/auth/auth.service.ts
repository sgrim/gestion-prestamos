import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, map, tap } from 'rxjs';

import { API_URL } from '../api';
import { LoginResponse, User } from '../models';

interface Session {
  token: string;
  expiresAt: string;
  user: User;
}

const STORAGE_KEY = 'prestamos.session';

/**
 * Sesión de la aplicación. Guarda el JWT en `sessionStorage` (se borra al cerrar la pestaña) y lo
 * expone mediante signals de solo lectura. El token nunca se lee desde fuera de este servicio salvo
 * por el interceptor.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly session = signal<Session | null>(readStoredSession());

  readonly user = computed(() => this.session()?.user ?? null);
  readonly token = computed(() => this.session()?.token ?? null);

  login(email: string, password: string): Observable<User> {
    return this.http.post<LoginResponse>(`${API_URL}/auth/login`, { email, password }).pipe(
      tap((res) => this.store({ token: res.token, expiresAt: res.expiresAt, user: res.user })),
      map((res) => res.user),
    );
  }

  logout(): void {
    this.clear();
    void this.router.navigate(['/login']);
  }

  /** `true` si hay sesión y el token no ha caducado. Limpia la sesión si caducó. */
  hasValidSession(): boolean {
    const current = this.session();
    if (!current) {
      return false;
    }
    if (Date.parse(current.expiresAt) <= Date.now()) {
      this.clear();
      return false;
    }
    return true;
  }

  /** Ruta de inicio según el rol del usuario autenticado. */
  homeUrl(): string {
    switch (this.user()?.role) {
      case 'ADMIN':
        return '/admin';
      case 'USER':
        return '/prestamos';
      default:
        return '/login';
    }
  }

  private store(session: Session): void {
    this.session.set(session);
    try {
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(session));
    } catch {
      // Almacenamiento no disponible (modo privado, cuota...): la sesión queda solo en memoria.
    }
  }

  private clear(): void {
    this.session.set(null);
    try {
      sessionStorage.removeItem(STORAGE_KEY);
    } catch {
      // nada que limpiar
    }
  }
}

function readStoredSession(): Session | null {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY);
    return raw ? (JSON.parse(raw) as Session) : null;
  } catch {
    return null;
  }
}
