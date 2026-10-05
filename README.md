# AgroCrew · Backend (API REST)

**AgroCrew** ayuda a pequeños productores agrícolas del Perú a decidir qué sembrar. La API cruza datos oficiales de suelos (MIDAGRI / SERFOR), riesgo de inundación (ANA) y clima (Open-Meteo), calcula qué cultivos son compatibles con cada terreno y usa inteligencia artificial para explicar el resultado en lenguaje sencillo.

| | |
|---|---|
| **API desplegada (Swagger)** | http://agrocrew-backend.us-east-1.elasticbeanstalk.com/swagger-ui.html |
| **Landing page** | https://jorize04.github.io/agrocrew-landing/ |
| **Curso** | Arquitectura de Aplicaciones Web (SI705) · UPC · 2026-20 |

---

## Funcionalidades

- **Usuarios y seguridad:** registro, inicio de sesión con JWT y tres roles (productor, asesor y administrador).
- **Predios:** registro y gestión de terrenos con departamento, provincia y distrito (ubigeo del INEI).
- **Suelo:** clasificación oficial de Capacidad de Uso Mayor del predio, traducida a lenguaje simple.
- **Clima:** temperatura y lluvia de los últimos 12 meses y pronóstico de 7 días.
- **Riesgo hídrico:** puntos críticos de inundación de la ANA a menos de 5 km del predio.
- **Evaluación y ranking:** un motor de reglas puntúa cada cultivo según suelo, altitud, temperatura, agua y riesgo.
- **Explicación con IA:** Google Gemini redacta el resultado en lenguaje sencillo, en español o inglés.
- **Alertas:** avisos automáticos por cercanía a zonas de inundación o lluvias intensas pronosticadas.
- **Reportes:** indicadores por zona para asesores técnicos y gobiernos locales.

## Inteligencia artificial

| | |
|---|---|
| **Problema que resuelve** | El resultado del motor de reglas (puntajes y factores) es técnico. La IA lo convierte en una explicación breve que un productor sin formación técnica puede entender. |
| **Modelo** | Google Gemini (`gemini-2.5-flash`, configurable), mediante su API REST. |
| **Datos que usa** | Solo los datos de la evaluación: suelo, altitud, clima, fuente de agua, riesgo y ranking. No se envían datos personales. |
| **Límites** | La IA solo redacta: el ranking lo decide el motor de reglas, que es determinístico. Si la IA no está configurada o falla, se usa una explicación con plantilla y la evaluación se completa igual. |

La explicación se genera en inglés cuando la petición llega con `Accept-Language: en`.

## Arquitectura

```
Cliente (Swagger / frontend)
        │  HTTP + JWT
        ▼
AWS Elastic Beanstalk ── Spring Boot 3.5 · Java 21
        │                   │
        │                   ├── SERFOR / MIDAGRI (suelos)
        │                   ├── ANA (puntos críticos)
        │                   ├── Open-Meteo (clima)
        │                   └── Google Gemini (IA)
        ▼
Amazon RDS · PostgreSQL
```

Las credenciales, la clave JWT y la API key de la IA se configuran como variables de entorno; nunca se guardan en el código.

## Tecnologías

Java 21 · Spring Boot 3.5 · Spring Data JPA · PostgreSQL · Spring Security 6 + JWT (jjwt) · Bean Validation · Lombok · Springdoc OpenAPI (Swagger) · JUnit 5 + Mockito · SLF4J / Logback · AWS Elastic Beanstalk · Amazon RDS · Google Gemini API.

## Ejecutar en local

**Requisitos:** JDK 21 y PostgreSQL 14 o superior.

1. Crea la base de datos:
   ```sql
   CREATE DATABASE agrocrew_db;
   ```
2. Revisa en `src/main/resources/application.properties` la contraseña local de PostgreSQL (`spring.datasource.password`), o define la variable de entorno `DB_PASSWORD`.
3. Opcional, para activar la IA: define la variable de entorno `IA_API_KEY` con una API key de [Google AI Studio](https://aistudio.google.com). Sin ella, la explicación se genera con plantilla.
4. Ejecuta `AgrocrewApplication` desde el IDE, o:
   ```bash
   mvn spring-boot:run
   ```
5. Abre http://localhost:8080/swagger-ui.html

En la primera ejecución se cargan automáticamente los roles, el ubigeo del Perú (25 departamentos, 196 provincias y 1892 distritos), los 5 grupos de Capacidad de Uso Mayor, 20 cultivos con sus requerimientos y el usuario administrador.

### Primeros pasos en Swagger

1. `POST /api/v1/auth/register` con `tipoUsuario` = `PRODUCTOR` o `ASESOR`.
2. `POST /api/v1/auth/login` y copia el `token`.
3. Pulsa **Authorize** y pega el token.
4. `GET /api/v1/usuarios/me` devuelve tu perfil; sin token responde 401.

La guía completa de pruebas está en [`docs/guia-pruebas-sprint1.md`](docs/guia-pruebas-sprint1.md).

## Endpoints principales

| Módulo | Endpoints | Roles |
|---|---|---|
| Autenticación | `/api/v1/auth/register`, `/api/v1/auth/login` | Público |
| Perfil | `/api/v1/usuarios/me` | Todos |
| Ubigeo | `/api/v1/ubigeo/**` | Todos |
| Predios | `/api/v1/predios` (CRUD) | Productor |
| Suelo, clima y riesgo | `/api/v1/predios/{id}/suelo`, `/clima`, `/riesgo` | Productor |
| Evaluaciones | `/api/v1/predios/{id}/evaluaciones`, `/api/v1/evaluaciones/{id}` | Productor |
| Alertas | `/api/v1/alertas` | Productor |
| Catálogo | `/api/v1/cultivos`, `/api/v1/grupos-cum` | Todos |
| Reportes | `/api/v1/reportes/**` | Asesor, Admin |
| Administración | `/api/v1/admin/usuarios`, `/admin/cultivos`, `/admin/puntos-criticos`, `/admin/integraciones` | Admin |

### Reportes (consultas personalizadas)

| Reporte | Consulta |
|---|---|
| Indicadores de la plataforma | Conteos |
| Predios por departamento | JPQL |
| Evaluaciones por grupo de suelo (filtro por departamento) | JPQL |
| Cultivos más recomendados (filtros y paginación) | JPQL |
| Predios por nivel de riesgo | JPQL |
| Puntos críticos por departamento | SQL nativo |
| Alertas por mes | SQL nativo |

## Motor de reglas

Cada cultivo recibe un puntaje de 0 a 100 según cinco factores: suelo (30), altitud (20), temperatura (20), agua (20) y riesgo hídrico (10). Con 75 o más la compatibilidad es **ALTA**; de 50 a 74, **MEDIA**; y por debajo de 50, **BAJA**. Un factor limitante de suelo, altitud o temperatura deja al cultivo en BAJA. El detalle está en [`docs/motor-reglas.md`](docs/motor-reglas.md).

## Pruebas

```bash
mvn test
```

Pruebas unitarias con JUnit 5 y Mockito para autenticación, predios, catálogo, motor de reglas, lectura de la clasificación de suelos, reportes y el servicio de IA.

## Variables de entorno

| Variable | Descripción | Por defecto |
|---|---|---|
| `DB_URL` | URL JDBC de PostgreSQL | `jdbc:postgresql://localhost:5432/agrocrew_db` |
| `DB_USERNAME`, `DB_PASSWORD` | Credenciales de la base de datos | Valores locales |
| `JWT_SECRET` | Clave Base64 de al menos 32 bytes | Clave de desarrollo |
| `JWT_EXPIRATION_MS` | Duración del token | 24 horas |
| `CORS_ORIGINS` | Orígenes permitidos del frontend, separados por coma | `localhost:4200`, `localhost:5173` |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Administrador inicial | Valores de desarrollo |
| `IA_API_KEY` | API key de Google Gemini | Sin IA (plantilla) |
| `IA_MODELO` | Modelo de Gemini | `gemini-2.5-flash` |
| `PORT` | Puerto del servidor | `8080` |

En producción todas estas variables se definen en Elastic Beanstalk, con valores distintos a los de desarrollo.

## Estructura del proyecto

```
pe.edu.upc.agrocrew
├── config         Configuración general, carga inicial de datos y tareas programadas
├── controllers    Endpoints REST
├── dto            Objetos de entrada y salida de la API
├── exceptions     Excepciones propias y manejador global de errores
├── integraciones  Clientes de SERFOR/MIDAGRI, ANA, Open-Meteo y Google Gemini
├── models         Entidades JPA y enums
├── repositories   Repositorios Spring Data JPA y consultas personalizadas
├── security       JWT, filtro de autenticación y configuración de seguridad
├── services       Interfaces, motor de reglas y servicio de IA
│   └── impl       Lógica de negocio
└── util           Utilidades (distancias, lectura de la clasificación de suelos)
```

## Flujo de trabajo

- `master`: versión estable, desplegada en AWS.
- `develop`: integración del sprint.
- `feature/<integrante>`: rama de trabajo de cada integrante.
- Los cambios llegan a `develop` y luego a `master` mediante Pull Requests.
- Commits con prefijos: `feat:`, `fix:`, `test:`, `docs:`.

## Equipo

| Integrante |
|---|
| Adrian Estrada Ochoa |
| Axel Yerico De La Cruz Huamán |
| José Rivera Zelaya |
| Mauricio Valverde Barrera |
| Ronaldo Torres Lizana |
| Saul Lujan Chu |

---

Proyecto académico. Las recomendaciones de AgroCrew son referenciales y no reemplazan la evaluación de un ingeniero agrónomo.
