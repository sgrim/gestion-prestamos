import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';

import { AuthService } from '../../core/auth/auth.service';
import { AppHeader } from '../../shared/app-header';
import { LoanRequestForm } from './loan-request-form';
import { MyLoans } from './my-loans';

@Component({
  selector: 'app-user-dashboard',
  imports: [AppHeader, LoanRequestForm, MyLoans],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-header [title]="'Bienvenido, ' + name()" />

    <nav class="tabs" role="tablist">
      <button class="tab tab-active" type="button" role="tab" aria-selected="true">Préstamos</button>
    </nav>

    <app-loan-request-form />
    <app-my-loans />
  `,
})
export class UserDashboard {
  private readonly auth = inject(AuthService);
  protected readonly name = computed(() => this.auth.user()?.fullName ?? 'Usuario');
}
