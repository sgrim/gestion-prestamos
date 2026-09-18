import { ChangeDetectionStrategy, Component, input } from '@angular/core';

import { LoanStatus, STATUS_LABEL } from '../core/models';

@Component({
  selector: 'app-status-badge',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<span class="badge" [class]="'badge-' + status().toLowerCase()">{{ label() }}</span>`,
})
export class StatusBadge {
  readonly status = input.required<LoanStatus>();
  protected label(): string {
    return STATUS_LABEL[this.status()];
  }
}
