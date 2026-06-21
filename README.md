# Gestión de Turnos Online — TP Final

API REST para la gestión de turnos online. Permite a negocios registrarse, configurar su agenda y servicios, y recibir reservas de clientes con lógica de seña integrada.

**Programación III · Grupo 8 · Tosunian · Rojas · Irianni**

---

## Stack

- Java 21 · Spring Boot 4.0.6
- Spring Security 7 + JWT (jjwt 0.11.5)
- Spring Data JPA · Hibernate 7 · MySQL
- Bean Validation · Lombok
- Swagger / OpenAPI 3 (springdoc 3.0.2)

---

## Requisitos

- JDK 21+
- MySQL 8+
- Maven 3.9+

---

## Configuración

Crear la base de datos:

```sql
CREATE DATABASE db_turnos;
```

Crear el archivo `src/main/resources/application.properties` (no está en el repo):

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/db_turnos
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration=86400000

app.admin.email=admin@turnosapp.com
app.admin.password=Admin1234!
app.admin.name=Admin
```

---

## Levantar el proyecto

```bash
./mvnw spring-boot:run
```

Al iniciar, el `DataSeeder` crea automáticamente:

- Los 4 roles del sistema: `OWNER`, `EMPLOYEE`, `CLIENT`, `ADMIN`
- 13 tipos de negocio predefinidos (PELUQUERIA, GYM, SPA, etc.)
- El usuario administrador con las credenciales de `application.properties`

---

## Documentación interactiva

Con la app corriendo, accedé a Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

Para probar endpoints protegidos: hacer login, copiar el token JWT y pegarlo en el botón **Authorize**.

---

## Autenticación

Todos los endpoints protegidos requieren el header:

```
Authorization: Bearer <token>
```

El token se obtiene en `POST /api/v1/auth/login`.

### Roles

| Rol        | Descripción                                                           |
| ---------- | --------------------------------------------------------------------- |
| `ADMIN`    | Superadmin. Gestiona usuarios, negocios y tipos de negocio.           |
| `OWNER`    | Dueño de negocio. Gestiona su negocio, servicios, agenda y empleados. |
| `EMPLOYEE` | Empleado de un negocio.                                               |
| `CLIENT`   | Cliente registrado. Puede ver sus turnos.                             |

---

## Endpoints

### Autenticación `/api/v1/auth`

| Método | Path              | Descripción                                  | Acceso      |
| ------ | ----------------- | -------------------------------------------- | ----------- |
| POST   | `/register`       | Registro de owner. Asigna rol OWNER.         | Público     |
| POST   | `/register/client`| Registro de cliente. Asigna rol CLIENT.      | Público     |
| POST   | `/login`          | Login. Devuelve JWT.                         | Público     |
| GET    | `/me`             | Usuario autenticado + roles.                 | Autenticado |

### Negocios `/api/v1/businesses`

| Método | Path                   | Descripción                    | Acceso  |
| ------ | ---------------------- | ------------------------------ | ------- |
| POST   | `/`                    | Crear negocio.                 | OWNER   |
| GET    | `/`                    | Listar todos los negocios.     | ADMIN   |
| GET    | `/{id}`                | Obtener negocio por ID.        | OWNER   |
| GET    | `/slug/{slug}`         | Perfil público del negocio.    | Público |
| GET    | `/mine`                | Negocio del owner autenticado. | OWNER   |
| PUT    | `/{id}`                | Editar negocio.                | OWNER   |
| DELETE | `/{id}`                | Soft delete de negocio.        | ADMIN   |
| POST   | `/{id}/types/{typeId}` | Asociar tipo al negocio.       | OWNER   |
| DELETE | `/{id}/types/{typeId}` | Desasociar tipo del negocio.   | OWNER   |

### Tipos de negocio `/api/v1/business-types`

| Método | Path    | Descripción               | Acceso  |
| ------ | ------- | ------------------------- | ------- |
| GET    | `/`     | Listar tipos disponibles. | Público |
| POST   | `/`     | Crear tipo.               | ADMIN   |
| PUT    | `/{id}` | Editar tipo.              | ADMIN   |
| DELETE | `/{id}` | Soft delete de tipo.      | ADMIN   |

### Servicios `/api/v1/businesses/{businessId}/services`

| Método | Path    | Descripción                   | Acceso  |
| ------ | ------- | ----------------------------- | ------- |
| GET    | `/`     | Listar servicios del negocio. | Público |
| POST   | `/`     | Crear servicio.               | OWNER   |
| PUT    | `/{id}` | Editar servicio.              | OWNER   |
| DELETE | `/{id}` | Soft delete de servicio.      | OWNER   |

### Agenda semanal `/api/v1/businesses/{businessId}/schedule`

| Método | Path | Descripción                    | Acceso |
| ------ | ---- | ------------------------------ | ------ |
| GET    | `/`  | Ver configuración de horarios. | OWNER  |
| PUT    | `/`  | Actualizar agenda semanal.     | OWNER  |

### Plantillas de turnos `/api/v1/businesses/{businessId}/appointment-schedules`

| Método | Path         | Descripción                                          | Acceso |
| ------ | ------------ | ---------------------------------------------------- | ------ |
| GET    | `/`          | Listar plantillas de slots.                          | OWNER  |
| POST   | `/`          | Crear plantilla de turno recurrente.                 | OWNER  |
| PUT    | `/{id}`      | Editar plantilla.                                    | OWNER  |
| DELETE | `/{id}`      | Eliminar plantilla.                                  | OWNER  |
| POST   | `/generate`  | Generar turnos reales a partir de las plantillas.    | OWNER  |

### Turnos `/api/v1`

| Método | Path                                   | Descripción                                 | Acceso      |
| ------ | -------------------------------------- | ------------------------------------------- | ----------- |
| GET    | `/businesses/{id}/appointments`        | Todos los turnos del negocio (paginado).    | OWNER       |
| GET    | `/businesses/{id}/appointments/today`  | Turnos del día.                             | OWNER       |
| GET    | `/businesses/{id}/appointments/public` | Slots disponibles para reservar.            | Público     |
| POST   | `/appointments/{id}/book`              | Reservar turno. Crea seña PENDING.          | Público     |
| POST   | `/appointments/{id}/pay-deposit`       | Confirmar seña. Turno pasa a BOOKED.        | Público     |
| PUT    | `/appointments/{id}/cancel`            | Cancelar turno.                             | Autenticado |
| PUT    | `/appointments/{id}/suspend`           | Suspender turno (negocio). Seña → REFUNDED. | OWNER       |
| DELETE | `/appointments/{id}`                   | Eliminar turno (solo UNBOOKED).             | OWNER       |
| GET    | `/users/me/appointments`               | Mis turnos como cliente registrado.         | Autenticado |

### Empleados `/api/v1`

| Método | Path                         | Descripción                               | Acceso |
| ------ | ---------------------------- | ----------------------------------------- | ------ |
| GET    | `/businesses/{id}/employees` | Listar empleados del negocio.             | OWNER  |
| POST   | `/businesses/{id}/employees` | Crear empleado (asigna rol EMPLOYEE).     | OWNER  |
| GET    | `/employees/{id}`            | Ver datos de empleado.                    | OWNER  |
| PUT    | `/employees/{id}`            | Editar empleado.                          | OWNER  |
| DELETE | `/employees/{id}`            | Desasociar empleado (quita rol EMPLOYEE). | OWNER  |

### Administración `/api/v1/admin/users`

| Método | Path           | Descripción                   | Acceso |
| ------ | -------------- | ----------------------------- | ------ |
| GET    | `/`            | Listar todos los usuarios.    | ADMIN  |
| GET    | `/{id}`        | Ver usuario + roles.          | ADMIN  |
| PATCH  | `/{id}/roles`  | Asignar / quitar roles.       | ADMIN  |
| PATCH  | `/{id}/active` | Activar / desactivar usuario. | ADMIN  |
| DELETE | `/{id}`        | Hard delete de usuario.       | ADMIN  |

### Estadísticas `/api/v1`

| Método | Path                                          | Descripción                              | Acceso              |
| ------ | --------------------------------------------- | ---------------------------------------- | ------------------- |
| GET    | `/businesses/{id}/stats`                      | Estadísticas globales del negocio.       | OWNER               |
| GET    | `/businesses/{id}/stats/employees/{empId}`    | Estadísticas individuales del empleado.  | OWNER / propio EMPLOYEE |
| GET    | `/admin/stats`                                | Métricas globales de la plataforma.      | ADMIN               |

**Query params disponibles en los tres endpoints:**

| Param      | Valores                          | Default  |
| ---------- | -------------------------------- | -------- |
| `period`   | `TODAY`, `WEEK`, `MONTH`, `YEAR` | `MONTH`  |
| `dateFrom` | fecha ISO (`2026-01-01`)         | —        |
| `dateTo`   | fecha ISO (`2026-03-31`)         | —        |

Si `dateFrom` y `dateTo` están presentes, tienen prioridad sobre `period`.

#### `GET /businesses/{id}/stats` — métricas del negocio

```json
{
  "period": { "from": "2026-06-01", "to": "2026-06-30" },
  "appointments": {
    "total": 120,
    "finished": 80,
    "upcoming": 15,
    "cancelled": 18,
    "suspended": 7,
    "occupancyRate": 79.17,
    "cancellationRate": 15.97
  },
  "revenue": {
    "totalFromFinished": 450000.00,
    "depositsCollected": 85000.00,
    "depositsRefunded": 12000.00,
    "depositsForfeited": 8000.00
  },
  "byService": [
    { "serviceName": "Corte", "appointmentCount": 50, "revenue": 180000.00 }
  ],
  "byEmployee": [
    { "employeeId": "uuid", "employeeName": "Juan", "appointmentCount": 40, "revenue": 200000.00 }
  ],
  "peakDays": [
    { "dayOfWeek": "FRIDAY", "appointmentCount": 25 }
  ],
  "uniqueClientsCount": 62
}
```

| Campo | Descripción |
| ----- | ----------- |
| `finished` | Turnos con `status = BOOKED` cuya `endDatetime` ya pasó |
| `upcoming` | Turnos con `status = BOOKED` cuya `endDatetime` es futura |
| `occupancyRate` | `(finished + upcoming) / total × 100` |
| `cancellationRate` | `cancelled / (finished + upcoming + cancelled) × 100` |
| `totalFromFinished` | Suma de `price` de los turnos finalizados |
| `depositsCollected` | Señas con estado `PAID` en el período |
| `depositsRefunded` | Señas devueltas por cancelación anticipada (≥ 24 hs) |
| `depositsForfeited` | Señas retenidas por cancelación tardía (< 24 hs) |
| `byService` | Turnos finalizados y revenue agrupados por servicio |
| `byEmployee` | Turnos finalizados y revenue agrupados por empleado |
| `peakDays` | Días de la semana ordenados por cantidad de turnos reservados |
| `uniqueClientsCount` | Clientes registrados distintos atendidos en el período |

#### `GET /businesses/{id}/stats/employees/{empId}` — métricas del empleado

```json
{
  "employeeId": "uuid",
  "employeeName": "Juan",
  "period": { "from": "2026-06-16", "to": "2026-06-22" },
  "appointments": {
    "total": 18,
    "finished": 12,
    "upcoming": 3,
    "cancelled": 3,
    "occupancyRate": 83.33
  },
  "hoursWorked": 6.0,
  "revenueGenerated": 54000.00,
  "byService": [
    { "serviceName": "Corte", "appointmentCount": 10, "revenue": 0 }
  ]
}
```

| Campo | Descripción |
| ----- | ----------- |
| `hoursWorked` | Suma de `durationMinutes` de los servicios en turnos finalizados, convertida a horas |
| `revenueGenerated` | Suma de `price` de los turnos finalizados propios del empleado |
| `byService` | Distribución de servicios realizados por el empleado en el período |

#### `GET /admin/stats` — métricas de la plataforma

```json
{
  "period": { "from": "2026-01-01", "to": "2026-12-31" },
  "totalBusinesses": 45,
  "activeBusinessesInPeriod": 38,
  "totalUsers": 320,
  "newUsersInPeriod": 85,
  "appointments": {
    "total": 5400,
    "finished": 3800,
    "cancelled": 610,
    "suspended": 90
  },
  "revenue": {
    "totalFromFinished": 12500000.00,
    "depositsCollected": 2300000.00,
    "depositsRefunded": 340000.00,
    "depositsForfeited": 180000.00
  },
  "topBusinesses": [
    { "businessName": "Peluquería Sol", "appointmentCount": 320, "revenue": 980000.00 }
  ],
  "userGrowth": [
    { "year": 2026, "month": 1, "newUsers": 12 },
    { "year": 2026, "month": 6, "newUsers": 28 }
  ]
}
```

| Campo | Descripción |
| ----- | ----------- |
| `activeBusinessesInPeriod` | Negocios con al menos un turno `BOOKED` en el período |
| `newUsersInPeriod` | Usuarios registrados durante el período filtrado |
| `topBusinesses` | Top 10 negocios por cantidad de turnos finalizados |
| `userGrowth` | Nuevos registros de usuarios por mes en los últimos 6 meses |

---

## Flujo de generación de turnos

Los turnos se generan a partir de **plantillas** (`AppointmentSchedule`). Cada plantilla define un bloque recurrente semanal: día de la semana, horario de inicio y fin, servicio y empleado opcional.

El generador (`AppointmentGeneratorService`) toma cada plantilla y crea slots `UNBOOKED` individuales según la duración del servicio, para los próximos N días (configurado en `scheduleDaysToCreate` del negocio).

**Ejemplo:** plantilla viernes 09:00-12:00, servicio de 30 min → genera turnos a las 09:00, 09:30, 10:00... para cada viernes dentro del rango.

La generación corre automáticamente **todos los días a las 01:00** y también puede dispararse manualmente con `POST /appointment-schedules/generate`.

---

## Flujo de reserva de un turno

```
1. GET  /businesses/{id}/appointments/public     → cliente ve slots disponibles
2. POST /appointments/{id}/book                  → cliente reserva (UNBOOKED → AWAITING_PAYMENT)
                                                    se crea seña PENDING automáticamente
3. POST /appointments/{id}/pay-deposit           → cliente confirma pago (→ BOOKED, seña → PAID)
```

---

## Flujo de estados de un turno

```
UNBOOKED → AWAITING_PAYMENT → BOOKED ─┬→ CANCELLED
                                       └→ SUSPENDED
```

### Regla de cancelación

| Situación                              | Seña             |
| -------------------------------------- | ---------------- |
| Cancelación con ≥ 24hs de anticipación | REFUNDED         |
| Cancelación con < 24hs de anticipación | FORFEITED        |
| Suspensión por el negocio              | REFUNDED siempre |

---

## Seguridad

- Autenticación stateless con JWT
- Contraseñas hasheadas con BCrypt
- Locking pesimista (`PESSIMISTIC_WRITE`) en la reserva de turnos para evitar doble booking concurrente
- Validación de propiedad: un owner solo puede gestionar su propio negocio y sus empleados
