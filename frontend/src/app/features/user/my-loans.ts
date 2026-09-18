import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit } from '@angular/core';

import { LoanStore } from '../../core/loans/loan.store';
import { StatusBadge } from '../../shared/status-badge';

@Component({
  selector: 'app-my-loans',
  imports: [CurrencyPipe, DatePipe, StatusBadge],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="card">
      <h2>Mis Préstamos</h2>

      @if (store.error(); as message) {
        <p class="alert alert-error" role="alert">{{ message }}</p>
      }

      @if (store.loading()) {
        <p class="muted" role="status">Cargando…</p>
      } @else if (store.loans().length === 0) {
        <p class="muted">Aún no has solicitado ningún préstamo.</p>
      } @else {
        <ul class="list">
          @for (loan of store.loans(); track loan.id) {
            <li class="list-item">
              <span>
                Monto: <strong>{{ loan.amount | currency: 'USD' : 'symbol' : '1.0-2' }}</strong>
                · {{ loan.termMonths }} {{ loan.termMonths === 1 ? 'mes' : 'meses' }}
                <small class="muted">· {{ loan.requestedAt | date: 'dd/MM/yyyy' }}</small>
              </span>
              <span>Estado: <app-status-badge [status]="loan.status" /></span>
            </li>
          }
        </ul>
      }
    </section>
  `,
})
export class MyLoans implements OnInit {
  protected readonly store = inject(LoanStore);

  ngOnInit(): void {
    this.store.loadMine();
  }
}
