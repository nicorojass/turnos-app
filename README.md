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

| Método | Path          | Descripción                     | Acceso |
| ------ | ------------- | ------------------------------- | ------ |
| GET    | `/`           | Listar todos los usuarios.      | ADMIN  |
| GET    | `/{id}`       | Ver usuario + roles.            | ADMIN  |
| PATCH  | `/{id}/roles` | Asignar / quitar roles.         | ADMIN  |
| PATCH  | `/{id}/active`| Activar / desactivar usuario.   | ADMIN  |
| DELETE | `/{id}`       | Hard delete de usuario.         | ADMIN  |

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
