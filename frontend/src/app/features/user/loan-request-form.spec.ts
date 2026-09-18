import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { LoanRequestForm, maxTwoDecimals } from './loan-request-form';
import { FormControl } from '@angular/forms';

describe('LoanRequestForm', () => {
  function setup() {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    const fixture = TestBed.createComponent(LoanRequestForm);
    // el formulario es protected: se accede a través del componente para las pruebas
    const form = (fixture.componentInstance as unknown as { form: LoanRequestForm['form'] }).form;
    return { fixture, form };
  }

  it('es inválido mientras esté vacío', () => {
    const { form } = setup();
    expect(form.invalid).toBe(true);
  });

  it.each([
    [50, 12],
    [1_000_001, 12],
    [1000, 0],
    [1000, 85],
    [1000, 6.5],
    [1000.123, 12],
  ])('rechaza monto=%s plazo=%s', (amount, termMonths) => {
    const { form } = setup();
    form.setValue({ amount, termMonths });
    expect(form.invalid).toBe(true);
  });

  it('acepta valores dentro de los límites', () => {
    const { form } = setup();
    form.setValue({ amount: 2500.5, termMonths: 36 });
    expect(form.valid).toBe(true);
  });

  it('muestra el mensaje de error del campo tras tocarlo', async () => {
    const { fixture, form } = setup();
    form.controls.amount.setValue(50);
    form.controls.amount.markAsTouched();
    await fixture.whenStable();

    const error = (fixture.nativeElement as HTMLElement).querySelector('.error');
    expect(error?.textContent).toContain('entre 100');
  });

  it('maxTwoDecimals ignora valores vacíos', () => {
    expect(maxTwoDecimals(new FormControl(null))).toBeNull();
    expect(maxTwoDecimals(new FormControl(10.99))).toBeNull();
    expect(maxTwoDecimals(new FormControl(10.999))).toEqual({ decimals: true });
  });
});
