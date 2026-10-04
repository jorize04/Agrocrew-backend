# AgroCrew – Backend (API REST)

API de AgroCrew para evaluar la aptitud agrícola de un predio (MIDAGRI), su riesgo hídrico (ANA) y su clima (Open-Meteo), y recomendar cultivos compatibles.

Curso: Arquitectura de Aplicaciones Web (SI705) – UPC, 2026-20.

## Tecnologías

Java 21 · Spring Boot 3.5 · Spring Data JPA · PostgreSQL · Spring Security 6 + JWT (jjwt) · Bean Validation · Lombok · Springdoc OpenAPI (Swagger) · JUnit 5 + Mockito · SLF4J/Logback · AWS.

## Cómo ejecutar en local

1. Tener instalado JDK 21 y PostgreSQL 14+.
2. Crear la base de datos:
   ```sql
   CREATE DATABASE agrocrew_db;
   ```
3. Crear un archivo `.env` en la carpeta del proyecto (junto a `pom.xml`) copiando `.env.example`, y poner tu contraseña de PostgreSQL en `DB_PASSWORD`. Ese archivo no se sube a GitHub.
4. Ejecutar `AgrocrewApplication` desde el IDE, o `mvn spring-boot:run`.
5. Abrir Swagger: http://localhost:8080/swagger-ui.html

Al iniciar por primera vez se cargan automáticamente: los roles `PRODUCTOR`, `ASESOR` y `ADMIN`; el ubigeo del Perú (25 departamentos, 196 provincias, 1892 distritos); los 5 grupos CUM y 20 cultivos iniciales (`resources/data/cultivos.csv`). Si en `.env` están `ADMIN_EMAIL` y `ADMIN_PASSWORD`, también se crea el usuario administrador.

### Probar la autenticación

1. `POST /api/v1/auth/register` con `tipoUsuario` = `PRODUCTOR` o `ASESOR`.
2. `POST /api/v1/auth/login` → copiar el `token`.
3. En Swagger pulsar **Authorize** y pegar el token.
4. `GET /api/v1/usuarios/me` debe devolver tu perfil. Sin token devuelve 401.

## Módulos del Sprint 1

| Módulo | Endpoints principales |
|---|---|
| Autenticación y usuarios | `/api/v1/auth/**`, `/api/v1/usuarios/me`, `/api/v1/admin/usuarios` |
| Ubigeo y predios | `/api/v1/ubigeo/**`, `/api/v1/predios` |
| Catálogo agronómico | `/api/v1/cultivos`, `/api/v1/grupos-cum`, `/api/v1/admin/cultivos` |
| Suelo, clima y riesgo | `/api/v1/predios/{id}/suelo`, `/clima`, `/riesgo`, `/api/v1/puntos-criticos` |
| Alertas | `/api/v1/alertas` |
| Evaluaciones (motor de reglas + explicación con IA) | `/api/v1/predios/{id}/evaluaciones`, `/api/v1/evaluaciones/{id}` |
| Reportes (ASESOR y ADMIN) | `/api/v1/reportes/**` |
| Administración de integraciones | `/api/v1/admin/puntos-criticos/**`, `/api/v1/admin/integraciones` |

- Fórmula del motor de reglas: `docs/motor-reglas.md`.
- Inteligencia artificial: Google Gemini redacta la explicación de cada evaluación (variable `IA_API_KEY`; sin ella se usa una plantilla). Ver `ExplicacionIaService`.
- Guía paso a paso para probar todo en Swagger: `docs/guia-pruebas-sprint1.md`.
- Integraciones externas configurables en `application.properties` (URLs de MIDAGRI, ANA y Open-Meteo, radio de riesgo, umbral de lluvia y horarios de las tareas automáticas).

## Estructura de paquetes

```
pe.edu.upc.agrocrew
├── config        Configuración general (Swagger, datos iniciales)
├── controllers   Endpoints REST (solo reciben/validan y llaman al servicio)
├── dto           Objetos de entrada/salida de la API (nunca exponer entidades)
├── exceptions    Excepciones propias y GlobalExceptionHandler
├── integraciones Clientes de servicios externos (MIDAGRI, ANA, Open-Meteo)
├── models        Entidades JPA (tablas) y enums
├── repositories  Interfaces Spring Data JPA
├── security      JWT, filtro, SecurityConfig, utilitario UsuarioActual
├── services      Interfaces de servicio y motor de reglas
│   └── impl      Implementaciones con la lógica de negocio
└── util          Utilidades (distancias, interpretación de la CUM)
```

### Convenciones del equipo

- Rutas: `/api/v1/<recurso-en-plural>` (ej. `/api/v1/predios`). Rutas de administración bajo `/api/v1/admin/**` (solo rol ADMIN).
- Tablas en español, snake_case y plural (`@Table(name = "predios")`). Atributos Java en camelCase: Hibernate los convierte a snake_case.
- Para errores lanzar `RecursoNoEncontradoException` (404), `ConflictoException` (409) o `ReglaNegocioException` (400). No devolver `null` ni construir respuestas de error a mano.
- Para obtener el usuario logueado dentro de un servicio: inyectar `UsuarioService` y llamar `obtenerUsuarioActual()`.
- Restringir por rol un endpoint: `@PreAuthorize("hasRole('ASESOR')")`.
- Logs con `@Slf4j`: `log.info` para eventos de negocio, `log.warn` para errores del cliente, `log.error` para fallos inesperados o de integraciones.

## Flujo de trabajo en Git

- `main`: versión desplegada en AWS. `develop`: integración del sprint.
- Cada tarea del Sprint Backlog se trabaja en su propia rama desde `develop`: `feature/T10-entidad-predio`.
- Mensajes de commit: `feat(predios): crear entidad Predio y DTOs (T10)`, `fix(auth): ...`, `test(...)`, `docs(...)`.
- Se integra a `develop` mediante Pull Request revisado por otro integrante.
- Nunca subir contraseñas ni el archivo `.env`.

## Variables de entorno (AWS)

| Variable | Descripción |
|---|---|
| `DB_URL` | `jdbc:postgresql://<host-rds>:5432/agrocrew_db` |
| `DB_USERNAME`, `DB_PASSWORD` | Credenciales de la base de datos |
| `JWT_SECRET` | Clave Base64 de al menos 32 bytes (distinta a la de desarrollo) |
| `JWT_EXPIRATION_MS` | Duración del token (por defecto 24 h) |
| `CORS_ORIGINS` | Orígenes del frontend separados por coma |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Administrador inicial (opcional) |
| `PORT` | Puerto del servidor (por defecto 8080) |
