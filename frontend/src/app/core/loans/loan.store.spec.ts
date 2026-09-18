import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Loan } from '../models';
import { LoanStore } from './loan.store';

const loan = (over: Partial<Loan> = {}): Loan => ({
  id: 1,
  userId: 1,
  userEmail: 'usuario@test.com',
  amount: 1000,
  termMonths: 12,
  status: 'PENDING',
  requestedAt: '2026-01-01T10:00:00Z',
  ...over,
});

describe('LoanStore', () => {
  let store: LoanStore;
  let http: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    store = TestBed.inject(LoanStore);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loadMine carga los préstamos del usuario', () => {
    store.loadMine();
    expect(store.loading()).toBe(true);

    http.expectOne('/api/loans/me').flush([loan(), loan({ id: 2, status: 'APPROVED' })]);

    expect(store.loading()).toBe(false);
    expect(store.loans().length).toBe(2);
    expect(store.pendingCount()).toBe(1);
  });

  it('loadAll con estado envía el filtro al backend', () => {
    store.loadAll('PENDING');
    http.expectOne('/api/loans?status=PENDING').flush([]);
    expect(store.loans()).toEqual([]);
  });

  it('request añade el préstamo nuevo al principio de la lista', () => {
    store.loadMine();
    http.expectOne('/api/loans/me').flush([loan({ id: 1 })]);

    store.request(5000, 24).subscribe();
    const req = http.expectOne('/api/loans');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ amount: 5000, termMonths: 24 });
    req.flush(loan({ id: 2, amount: 5000, termMonths: 24 }));

    expect(store.loans().map((l) => l.id)).toEqual([2, 1]);
  });

  it('decide actualiza solo el préstamo afectado', () => {
    store.loadAll();
    http.expectOne('/api/loans').flush([loan({ id: 1 }), loan({ id: 2 })]);

    store.decide(1, 'approve');
    expect(store.busyId()).toBe(1);
    const req = http.expectOne('/api/loans/1/approve');
    expect(req.request.method).toBe('PATCH');
    req.flush(loan({ id: 1, status: 'APPROVED' }));

    expect(store.busyId()).toBeNull();
    expect(store.loans().find((l) => l.id === 1)?.status).toBe('APPROVED');
    expect(store.loans().find((l) => l.id === 2)?.status).toBe('PENDING');
  });

  it('si el backend responde 409 muestra el detalle y refresca la lista', () => {
    store.loadAll();
    http.expectOne('/api/loans').flush([loan({ id: 1 })]);

    store.decide(1, 'reject');
    http
      .expectOne('/api/loans/1/reject')
      .flush({ detail: 'El préstamo 1 ya fue resuelto' }, { status: 409, statusText: 'Conflict' });

    http.expectOne('/api/loans').flush([loan({ id: 1, status: 'APPROVED' })]);

    expect(store.error()).toBe('El préstamo 1 ya fue resuelto');
    expect(store.loans()[0].status).toBe('APPROVED');
  });
});
