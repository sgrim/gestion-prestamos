# Trabajo pendiente (a completar por el autor)

Estas piezas se dejaron a propósito sin terminar. Todas compilan y el resto del sistema funciona sin
ellas; cada una tiene un `TODO(candidato)` en el código con pistas.

> Sugerencia: hazlas tú, con tus propias palabras. Son pequeñas, pero te obligan a recorrer todas las
> capas de la arquitectura, que es justo lo que suele preguntarse en la revisión.

## 1. Actualizar y eliminar usuarios (backend)

`PUT /api/users/{id}` y `DELETE /api/users/{id}` responden hoy `501 Not Implemented`.

- [ ] `UserService.update` — añadir un método de negocio en `User` (es un `record` inmutable, devuelve una copia).
- [ ] `UserService.delete` — decidir la política: ¿baja lógica (`active = false`) o física? Un usuario con
      préstamos no se puede borrar por la FK; ¿qué debe pasar? Documenta tu decisión en el README.
- [ ] Regla adicional razonable: un admin no debería poder desactivarse/borrarse a sí mismo.
- [ ] Tests unitarios de ambos casos (`UserServiceTest`) y un caso en `LoanApiIntegrationTest`.

Archivos: `application/service/UserService.java`, `domain/model/User.java`,
`infrastructure/adapter/in/web/UserController.java`.

## 2. Tests de rechazo de préstamos (backend)

En `DecideLoanServiceTest` hay dos tests marcados `@Disabled` (`rejectPersistsTheDecisionAndNotifies`,
`rejectFailsWhenLoanWasAlreadyApproved`). Quita el `@Disabled` y escríbelos siguiendo el patrón de los
tests de `approve`.

## 3. Filtro por estado en el panel de administración (frontend)

`AdminDashboard` muestra todas las solicitudes. Añade un selector (Todos / Pendientes / Aprobados /
Rechazados). El backend ya soporta `?status=` y `LoanStore.loadAll(status)` ya lo envía: falta la UI, una
`signal` con el filtro y un test.

## 4. Ideas opcionales

- Calcular y mostrar la cuota mensual estimada al solicitar (tasa fija configurable).
- Paginación en `GET /api/loans`.
- Documentar la API con springdoc-openapi.
