import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Loan } from '../../core/models';
import { AdminDashboard } from './admin-dashboard';

const loan = (id: number, status: Loan['status']): Loan => ({
  id,
  userId: 2,
  userEmail: 'usuario@test.com',
  amount: 1000,
  termMonths: 12,
  status,
  requestedAt: '2026-01-01T10:00:00Z',
});

describe('AdminDashboard', () => {
  let http: HttpTestingController;

  function setup() {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(AdminDashboard);
    fixture.detectChanges(); // dispara ngOnInit
    return fixture;
  }

  const tabs = (el: HTMLElement) => Array.from(el.querySelectorAll<HTMLButtonElement>('.tab'));

  afterEach(() => http.verify());

  it('carga todas las solicitudes al iniciar y muestra los botones solo en las pendientes', async () => {
    const fixture = setup();
    http.expectOne('/api/loans').flush([loan(1, 'PENDING'), loan(2, 'APPROVED')]);
    await fixture.whenStable();

    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelectorAll('.list-item').length).toBe(2);
    expect(el.querySelectorAll('.actions').length).toBe(1);
    expect(el.textContent).toContain('1 solicitud pendiente');
  });

  it('el filtro por estado vuelve a consultar el backend con ?status=', async () => {
    const fixture = setup();
    http.expectOne('/api/loans').flush([loan(1, 'PENDING'), loan(2, 'APPROVED')]);
    await fixture.whenStable();

    const el = fixture.nativeElement as HTMLElement;
    tabs(el)
      .find((t) => t.textContent?.trim() === 'Aprobados')!
      .click();
    http.expectOne('/api/loans?status=APPROVED').flush([loan(2, 'APPROVED')]);
    await fixture.whenStable();

    expect(el.querySelectorAll('.list-item').length).toBe(1);
    expect(el.textContent).toContain('1 resultado');
    const active = tabs(el).filter((t) => t.classList.contains('tab-active'));
    expect(active.map((t) => t.textContent?.trim())).toEqual(['Aprobados']);
  });
});
