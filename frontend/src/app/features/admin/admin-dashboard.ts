import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, OnInit } from '@angular/core';

import { AuthService } from '../../core/auth/auth.service';
import { LoanStore } from '../../core/loans/loan.store';
import { AppHeader } from '../../shared/app-header';
import { StatusBadge } from '../../shared/status-badge';

@Component({
  selector: 'app-admin-dashboard',
  imports: [AppHeader, StatusBadge, CurrencyPipe, DatePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './admin-dashboard.html',
})
export class AdminDashboard implements OnInit {
  private readonly auth = inject(AuthService);
  protected readonly store = inject(LoanStore);

  protected readonly name = computed(() => this.auth.user()?.fullName ?? 'Admin');

  ngOnInit(): void {
    this.store.loadAll();
  }

  // TODO(candidato): añade un filtro por estado (Todos / Pendientes / Aprobados / Rechazados).
  //  Pistas: `store.loadAll(status)` ya envía `?status=` al backend; solo falta la UI,
  //  una signal con el filtro seleccionado y un test que verifique que se recarga la lista.
}
