import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  provideRouter,
  RouterStateSnapshot,
  UrlTree,
} from '@angular/router';

import { Role } from '../models';
import { AuthService } from './auth.service';
import { authGuard, guestGuard, roleGuard } from './guards';

describe('guards', () => {
  let auth: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    auth = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  function run(guard: CanActivateFn) {
    return TestBed.runInInjectionContext(() =>
      guard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );
  }

  function loginAs(role: Role, expiresInMs = 60_000): void {
    auth.login('x@test.com', '123').subscribe();
    http.expectOne('/api/auth/login').flush({
      token: 'jwt',
      tokenType: 'Bearer',
      expiresAt: new Date(Date.now() + expiresInMs).toISOString(),
      user: { id: 1, email: 'x@test.com', fullName: 'X', role, active: true },
    });
  }

  it('authGuard envía al login si no hay sesión', () => {
    const result = run(authGuard) as UrlTree;
    expect(result.toString()).toBe('/login');
  });

  it('authGuard deja pasar con sesión válida', () => {
    loginAs('USER');
    expect(run(authGuard)).toBe(true);
  });

  it('authGuard descarta una sesión con el token caducado', () => {
    loginAs('USER', -1000);
    expect((run(authGuard) as UrlTree).toString()).toBe('/login');
    expect(auth.user()).toBeNull();
  });

  it('roleGuard(ADMIN) devuelve a un usuario normal a su pantalla', () => {
    loginAs('USER');
    expect((run(roleGuard('ADMIN')) as UrlTree).toString()).toBe('/prestamos');
  });

  it('roleGuard(ADMIN) deja pasar a un administrador', () => {
    loginAs('ADMIN');
    expect(run(roleGuard('ADMIN'))).toBe(true);
  });

  it('guestGuard redirige al panel del admin si ya hay sesión', () => {
    loginAs('ADMIN');
    expect((run(guestGuard) as UrlTree).toString()).toBe('/admin');
  });
});
