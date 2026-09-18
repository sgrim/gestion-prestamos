# Gestión de Préstamos Bancarios

Simulación de un sistema de préstamos: los usuarios solicitan préstamos y consultan su estado; los
administradores los aprueban o rechazan. Prueba técnica — Spring Boot + Angular + PostgreSQL.

## Puesta en marcha

Solo necesitas Docker y `make`.

```bash
make up        # construye y levanta db + backend + frontend
```

| Servicio | URL |
|----------|-----|
| Frontend | http://localhost:4200 |
| API (salud) | http://localhost:8081/actuator/health |
| PostgreSQL | `localhost:5433` (`make db-shell`) |

Usuarios de demostración (clave `123` para ambos):

| Rol | Email |
|-----|-------|
| Usuario | `usuario@test.com` |
| Admin | `admin@test.com` |

Otros comandos: `make help` · `make logs` · `make test` · `make down` · `make clean` (borra los datos).

> Los puertos y secretos se cambian copiando `.env.example` a `.env` (`make env`).
> Los puertos por defecto (8081, 5433) evitan choques con servicios habituales en 8080/5432.

## Stack

| Capa | Tecnología |
|------|-----------|
| Backend | Java 21, Spring Boot 4.1, Spring Web MVC, Spring Data JPA (Hibernate), Hibernate Validator, Spring Security (JWT), Spring WebFlux (`WebClient`), Flyway |
| Caché | Ehcache 3 vía JCache |
| Base de datos | PostgreSQL 18 |
| Frontend | Angular 22 (standalone, signals, Reactive Forms, Guards, interceptor JWT) |
| Tests | JUnit 5, Mockito, Spring Boot Test + MockMvc (H2), ArchUnit; Vitest en el frontend |
| Infra | Docker Compose, Makefile, nginx |

## Arquitectura hexagonal (backend)

```
backend/src/main/java/com/makers/prestamos
├── domain/                     # Núcleo: sin Spring ni JPA
│   ├── model/                  #   Loan, User, LoanStatus, Role  (reglas de negocio)
│   └── exception/              #   Excepciones de negocio
├── application/                # Casos de uso
│   ├── port/in/                #   Puertos de entrada  (qué ofrece la app)
│   ├── port/out/               #   Puertos de salida   (qué necesita la app)
│   └── service/                #   Implementación de los casos de uso (@Transactional)
└── infrastructure/             # Adaptadores y configuración
    ├── adapter/in/web/         #   REST: controladores, DTOs, manejo de errores
    ├── adapter/out/persistence/#   JPA: entidades, repositorios, mapeo, caché
    ├── adapter/out/security/   #   JWT (emisión) y BCrypt
    ├── adapter/out/notification/#  WebClient (webhook opcional)
    └── config/                 #   Seguridad, caché, propiedades, datos demo
```

La regla es que **las dependencias apuntan hacia adentro**: `infrastructure → application → domain`.
No es solo una convención: `HexagonalArchitectureTest` (ArchUnit) hace que el build falle si alguien
la rompe (p. ej. si el dominio importa Spring o un servicio importa un controlador).

### Reglas de negocio (viven en `Loan`)

- Monto entre 100 y 1.000.000; plazo entre 1 y 84 meses.
- Toda solicitud nace `PENDING`; solo se puede decidir una vez (`APPROVED` / `REJECTED`).
- Un usuario solo ve sus propios préstamos; un admin ve todos.

## API

| Método | Ruta | Acceso |
|--------|------|--------|
| `POST` | `/api/auth/login` | público |
| `POST` | `/api/loans` | autenticado — solicita un préstamo `{amount, termMonths}` |
| `GET` | `/api/loans/me` | autenticado — mis préstamos |
| `GET` | `/api/loans/{id}` | dueño o admin |
| `GET` | `/api/loans?status=PENDING` | **admin** |
| `PATCH` | `/api/loans/{id}/approve` · `/reject` | **admin** |
| `GET/POST` | `/api/users`, `/api/users/{id}` | **admin** |
| `PUT` | `/api/users/{id}` | **admin** — nombre, rol y estado |
| `DELETE` | `/api/users/{id}` | **admin** — baja lógica (el usuario queda inactivo) |

Los errores usan `ProblemDetail` (RFC 9457): `400` validación (con `errors` por campo), `401`
credenciales/token, `403` sin permisos, `404` no existe, `409` ya resuelto o email duplicado,
`422` regla de negocio.

## Decisiones técnicas

- **Transacciones**: `DecideLoanService` es `@Transactional` y lee el préstamo con `PESSIMISTIC_WRITE`
  (`select ... for update`). Así dos administradores no pueden decidir el mismo préstamo a la vez: el
  segundo espera y luego recibe `409`. La BD refuerza la regla con un `CHECK` sobre la decisión.
- **Caché**: `loansByUser` y `loansById` (Ehcache, TTL 5 min) viven en el adaptador de persistencia,
  no en los casos de uso: es un detalle de infraestructura. Se invalidan al guardar y el gestor es
  *transaction-aware*, así la invalidación ocurre tras el commit. `Loan` es un `record` inmutable, por
  lo que es seguro cachearlo. Limitación: la caché es local a cada instancia; con varias réplicas
  habría que pasar a una caché distribuida.
- **WebFlux**: la API es CRUD sobre JPA (bloqueante), así que MVC es la elección correcta. WebFlux se
  usa solo donde aporta: el `WebClient` que notifica un webhook al resolver un préstamo, sin bloquear
  y después del commit. Se activa con `APP_NOTIFICATIONS_WEBHOOK_URL`; si no, solo se registra en log.
- **Seguridad**: API stateless con JWT (HS256, Nimbus). El rol viaja en el token y se aplica a nivel de
  URL (`SecurityConfig`) **y** con `@PreAuthorize` en los controladores. Las contraseñas usan BCrypt y el
  login devuelve el mismo error tanto si el email no existe como si la clave es incorrecta.
- **Esquema**: lo crea Flyway (`V1__esquema_inicial.sql`); Hibernate solo valida (`ddl-auto: validate`).
- **Datos demo**: `DemoDataInitializer` crea los usuarios y préstamos de ejemplo solo si la BD está
  vacía y `APP_SEED_ENABLED=true`. Desactívalo fuera de desarrollo.

## Frontend (Angular)

Ver [`frontend/README.md`](frontend/README.md) para su estructura.

## Gestión de usuarios

- `DELETE` es una **baja lógica**: el usuario pasa a `active = false` y ya no puede iniciar sesión, pero se
  conserva su historial de préstamos (la FK impide el borrado físico y los datos financieros no deberían perderse).
  Es idempotente.
- Un administrador **no puede** desactivarse, borrarse ni cambiarse el rol a sí mismo (`409`), para no dejar el
  sistema sin administradores por accidente.
- Limitación conocida: un JWT ya emitido sigue siendo válido hasta que caduca (2 h por defecto) aunque el usuario
  se dé de baja. Para revocación inmediata habría que comprobar `active` en cada petición o usar una lista de
  revocación.

## Ideas futuras

- Calcular y mostrar la cuota mensual estimada al solicitar (tasa fija configurable).
- Paginación en `GET /api/loans`.
- Documentar la API con springdoc-openapi.
- Caché distribuida (p. ej. Redis) si se despliega con varias réplicas.
