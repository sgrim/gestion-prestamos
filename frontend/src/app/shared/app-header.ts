import { ChangeDetectionStrategy, Component, inject, input } from '@angular/core';

import { AuthService } from '../core/auth/auth.service';

@Component({
  selector: 'app-header',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <header class="topbar">
      <h1>{{ title() }}</h1>
      <button class="btn" type="button" (click)="auth.logout()">Cerrar Sesión</button>
    </header>
  `,
})
export class AppHeader {
  protected readonly auth = inject(AuthService);
  readonly title = input.required<string>();
}
