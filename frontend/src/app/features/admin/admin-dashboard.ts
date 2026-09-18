import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';

import { AuthService } from '../../core/auth/auth.service';
import { LoanStore } from '../../core/loans/loan.store';
import { LoanStatus } from '../../core/models';
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

  protected readonly filters: { label: string; value?: LoanStatus }[] = [
    { label: 'Todos' },
    { label: 'Pendientes', value: 'PENDING' },
    { label: 'Aprobados', value: 'APPROVED' },
    { label: 'Rechazados', value: 'REJECTED' },
  ];
  protected readonly filter = signal<LoanStatus | undefined>(undefined);

  ngOnInit(): void {
    this.store.loadAll();
  }

  protected select(status?: LoanStatus): void {
    this.filter.set(status);
    this.store.loadAll(status);
  }
}
