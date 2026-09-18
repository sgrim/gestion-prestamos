import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, computed, effect, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { API_URL, errorMessage } from '../api';
import { AuthService } from '../auth/auth.service';
import { Loan, LoanStatus } from '../models';

export type Decision = 'approve' | 'reject';

/**
 * Estado de los préstamos para toda la app, expuesto como signals de solo lectura.
 * Los componentes no tocan el estado directamente: solo llaman a estos métodos.
 */
@Injectable({ providedIn: 'root' })
export class LoanStore {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);

  private readonly _loans = signal<Loan[]>([]);
  private readonly _loading = signal(false);
  private readonly _error = signal<string | null>(null);
  private readonly _busyId = signal<number | null>(null);

  readonly loans = this._loans.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();
  /** Id del préstamo que se está aprobando/rechazando (para deshabilitar sus botones). */
  readonly busyId = this._busyId.asReadonly();
  readonly pendingCount = computed(() => this._loans().filter((l) => l.status === 'PENDING').length);

  constructor() {
    // Al cerrar sesión no debe quedar ningún dato del usuario anterior en memoria.
    effect(() => {
      if (!this.auth.user()) {
        this.reset();
      }
    });
  }

  /** Préstamos del usuario autenticado. */
  loadMine(): void {
    this.load(this.http.get<Loan[]>(`${API_URL}/loans/me`));
  }

  /** Todos los préstamos (admin). El backend admite filtrar por estado. */
  loadAll(status?: LoanStatus): void {
    const params = status ? new HttpParams().set('status', status) : undefined;
    this.load(this.http.get<Loan[]>(`${API_URL}/loans`, { params }));
  }

  request(amount: number, termMonths: number): Observable<Loan> {
    return this.http
      .post<Loan>(`${API_URL}/loans`, { amount, termMonths })
      .pipe(tap((created) => this._loans.update((list) => [created, ...list])));
  }

  decide(id: number, decision: Decision): void {
    this._busyId.set(id);
    this._error.set(null);
    this.http.patch<Loan>(`${API_URL}/loans/${id}/${decision}`, null).subscribe({
      next: (updated) => {
        this._loans.update((list) => list.map((l) => (l.id === id ? { ...l, ...updated } : l)));
        this._busyId.set(null);
      },
      error: (err: unknown) => {
        this._busyId.set(null);
        // Si otro admin ya lo resolvió (409), refrescamos para mostrar el estado real.
        // El refresco limpia el error, así que el mensaje se fija después.
        this.loadAll();
        this._error.set(errorMessage(err));
      },
    });
  }

  private load(request$: Observable<Loan[]>): void {
    this._loading.set(true);
    this._error.set(null);
    request$.subscribe({
      next: (loans) => {
        this._loans.set(loans);
        this._loading.set(false);
      },
      error: (err: unknown) => {
        this._error.set(errorMessage(err, 'No se pudieron cargar los préstamos'));
        this._loading.set(false);
      },
    });
  }

  private reset(): void {
    this._loans.set([]);
    this._error.set(null);
    this._loading.set(false);
    this._busyId.set(null);
  }
}
