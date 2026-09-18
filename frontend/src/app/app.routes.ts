import { Routes } from '@angular/router';

import { authGuard, guestGuard, homeRedirectGuard, roleGuard } from './core/auth/guards';

export const routes: Routes = [
  { path: '', pathMatch: 'full', canActivate: [homeRedirectGuard], children: [] },
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/login/login-page').then((m) => m.LoginPage),
    title: 'Iniciar sesión',
  },
  {
    path: 'prestamos',
    canActivate: [authGuard, roleGuard('USER')],
    loadComponent: () => import('./features/user/user-dashboard').then((m) => m.UserDashboard),
    title: 'Mis préstamos',
  },
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard('ADMIN')],
    loadComponent: () => import('./features/admin/admin-dashboard').then((m) => m.AdminDashboard),
    title: 'Gestionar solicitudes',
  },
  { path: '**', redirectTo: '' },
];
