# Frontend — Gestión de Préstamos

Angular 22 (standalone components, signals, zoneless) con Reactive Forms.

```bash
make run-front      # desde la raíz: ng serve con proxy a http://localhost:8081
make test-front     # tests (Vitest) dentro de un contenedor
```

## Estructura

```
src/app
├── core/
│   ├── models.ts              # tipos y límites del dominio
│   ├── api.ts                 # URL base y traducción de errores del backend
│   ├── auth/
│   │   ├── auth.service.ts    # sesión (signals) + JWT en sessionStorage
│   │   ├── auth.interceptor.ts# añade Bearer y cierra sesión ante 401
│   │   └── guards.ts          # authGuard, guestGuard, roleGuard, homeRedirectGuard
│   └── loans/loan.store.ts    # estado de préstamos (signals de solo lectura)
├── features/
│   ├── login/                 # formulario de acceso
│   ├── user/                  # solicitar préstamo + mis préstamos
│   └── admin/                 # gestionar solicitudes (aprobar / rechazar)
└── shared/                    # cabecera y badge de estado
```

## Decisiones

- **Estado**: un servicio con signals (`LoanStore`, `AuthService`) en lugar de NgRx: el estado es pequeño y
  local; NgRx añadiría más código que valor. Los componentes solo leen signals y llaman a métodos.
- **Seguridad**: los guards solo mejoran la experiencia; la autorización real la aplica el backend. El JWT
  vive en `sessionStorage` (se borra al cerrar la pestaña) y se elimina al recibir un 401 o al caducar.
- **Formularios**: validación reactiva con los mismos límites que el dominio (monto 100–1.000.000, plazo 1–84).
- **Origen único**: en Docker nginx sirve el front y proxifica `/api` al backend, así que no hay CORS.
