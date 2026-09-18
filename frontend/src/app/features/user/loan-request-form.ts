import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';

import { errorMessage } from '../../core/api';
import { LoanStore } from '../../core/loans/loan.store';
import { LOAN_LIMITS } from '../../core/models';

/** El monto admite como máximo 2 decimales (como el backend). */
export function maxTwoDecimals(control: AbstractControl): ValidationErrors | null {
  const value = control.value;
  if (value === null || value === '' || Number.isNaN(Number(value))) {
    return null;
  }
  return Math.abs(Math.round(Number(value) * 100) - Number(value) * 100) < 1e-6
    ? null
    : { decimals: true };
}

@Component({
  selector: 'app-loan-request-form',
  imports: [ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './loan-request-form.html',
})
export class LoanRequestForm {
  private readonly store = inject(LoanStore);

  protected readonly limits = LOAN_LIMITS;

  protected readonly form = inject(FormBuilder).group({
    amount: [
      null as number | null,
      [
        Validators.required,
        Validators.min(LOAN_LIMITS.minAmount),
        Validators.max(LOAN_LIMITS.maxAmount),
        maxTwoDecimals,
      ],
    ],
    termMonths: [
      null as number | null,
      [
        Validators.required,
        Validators.min(LOAN_LIMITS.minTerm),
        Validators.max(LOAN_LIMITS.maxTerm),
        Validators.pattern(/^\d+$/),
      ],
    ],
  });

  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly success = signal(false);

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { amount, termMonths } = this.form.getRawValue();
    this.submitting.set(true);
    this.error.set(null);
    this.success.set(false);

    this.store.request(Number(amount), Number(termMonths)).subscribe({
      next: () => {
        this.form.reset();
        this.success.set(true);
        this.submitting.set(false);
      },
      error: (err: unknown) => {
        this.error.set(errorMessage(err, 'No se pudo enviar la solicitud'));
        this.submitting.set(false);
      },
    });
  }

  protected invalid(name: 'amount' | 'termMonths'): boolean {
    const control = this.form.controls[name];
    return control.invalid && (control.touched || control.dirty);
  }
}
