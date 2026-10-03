# Guía de pruebas del backend – Sprint 1

Sigue los pasos en orden. En cada paso se indica qué debe salir. Toma captura de cada respuesta: sirven como evidencia para la sección 4.2.X.4 del informe.

Recuerda cómo se prueba cada endpoint en Swagger (`http://localhost:8080/swagger-ui.html`): abrir la barra del endpoint → **Try it out** → llenar los datos → **Execute** → mirar **Server response** (no la sección "Responses" de abajo, que es solo documentación).

---

## Parte 0. Arranque

Al ejecutar `AgrocrewApplication` por primera vez con esta versión, en la consola deben aparecer las tablas nuevas (avisos amarillos de "no existe la restricción... omitiendo", son normales) y al final `Started AgrocrewApplication`.

---

## Parte 1. Como administrador

**1.1. Login del administrador.** `POST /api/v1/auth/login`
```json
{ "email": "admin@agrocrew.pe", "password": "Admin12345" }
```
Esperado: **200** y `"rol": "ADMIN"`. Copia el token → botón **Authorize** → pega → Authorize → Close.

**1.2. Catálogo.** `GET /api/v1/cultivos` → **200** con 20 cultivos. `GET /api/v1/grupos-cum` → **200** con 5 grupos (A, C, P, F, X).

**1.3. Registrar un cultivo.** `POST /api/v1/admin/cultivos` con el ejemplo que trae Swagger, cambiando el nombre a `"Olluco"`. Esperado: **201**.
Vuelve a enviar lo mismo. Esperado: **409** "Ya existe un cultivo con el nombre Olluco".
Cambia el nombre a `"Mashua"` y pon `"altitudMin": 5000`. Esperado: **400** "La altitud mínima no puede ser mayor que la máxima".

**1.4. Sincronizar con la ANA.** `POST /api/v1/admin/puntos-criticos/sincronizar` (sin datos).
- Si sale **200**: muestra cuántos puntos se descargaron de la capa 125 de la ANA (puede tardar varios segundos). En la consola aparece una línea `Campos de Puntos Críticos ANA: [...]`; cópiala y guárdala, sirve para revisar que los campos se leyeron bien.
- Si sale **503**: el servicio de la ANA no respondió. No es un error del programa; continúa con el paso 1.5.

**1.5. Registrar un punto crítico de prueba** cerca del predio de José. `POST /api/v1/admin/puntos-criticos`
```json
{
  "codigo": "MANUAL-001",
  "descripcion": "Desborde del río Mantaro en el sector Pilcomayo",
  "tipoPeligro": "DESBORDE",
  "nivelRiesgo": "ALTO",
  "latitud": -12.0600,
  "longitud": -75.2200
}
```
Esperado: **201**. Al registrarlo, el sistema genera automáticamente una alerta para el predio de José (está a unos 1.4 km).

**1.6. Bitácora de integraciones.** `GET /api/v1/admin/integraciones` → **200** con las llamadas hechas a la ANA (con `exito` true o false y la duración en ms).

---

## Parte 2. Como productor (José Rivera)

**2.1. Cambiar de usuario.** Botón **Authorize** → **Logout**. Luego login con José Rivera:
```json
{ "email": "jose.rivera@test.com", "password": "clave1234" }
```
Copia el token → **Authorize**.

**2.2. Mis predios.** `GET /api/v1/predios` → **200** con "Chacra La Esperanza". Anota su `id` (debería ser 1). En los siguientes pasos, `predioId` = ese número.

**2.3. Suelo.** `GET /api/v1/predios/{predioId}/suelo` → **200** con `"disponible": false` y un mensaje explicando que no hay clasificación oficial para esa ubicación. Es lo esperado: los estudios de Capacidad de Uso Mayor (capa del SERFOR/MIDAGRI) no cubren El Tambo. En el paso 2.12 se prueba un predio que sí tiene clasificación.
Si saliera **503**, el servicio del SERFOR no respondió; revisa `GET /api/v1/admin/integraciones` (como admin) para ver el error.

**2.4. Clima.** `GET /api/v1/predios/{predioId}/clima` → **200** con `temperaturaMedia`, `precipitacionAnualMm` y 7 días de `pronostico`.

**2.5. Riesgo.** `GET /api/v1/predios/{predioId}/riesgo` → **200** con `"nivelRiesgo": "ALTO"` y el punto MANUAL-001 como `puntoMasCercano`.

**2.6. Alertas.** `GET /api/v1/alertas/no-leidas/cantidad` → `{"noLeidas": 1}` (o más si la ANA trajo puntos de riesgo alto cercanos). `GET /api/v1/alertas` → la alerta con título, mensaje y acciones sugeridas. Anota su `id`.

**2.7. Evaluación general.** `POST /api/v1/predios/{predioId}/evaluaciones` con el cuerpo:
```json
{}
```
Esperado: **201** con:
- `estado`: COMPLETADA (o PARCIAL si algún servicio externo falló; en ese caso `fuentesFaltantes` dice cuál).
- `ranking`: cultivos de sierra (papa, haba, quinua...). Como el predio tiene un punto crítico de riesgo ALTO cerca y esos cultivos toleran poco las inundaciones, su compatibilidad sale **MEDIA** y no ALTA: es el comportamiento esperado del motor.
- Cada cultivo trae sus `factores` (SUELO, ALTITUD, TEMPERATURA, AGUA, RIESGO_HIDRICO) con su efecto. Como El Tambo no tiene clasificación de suelo, el factor SUELO sale NEUTRO ("Sin clasificación").
- `explicacion` en lenguaje sencillo y `aviso` de alcance referencial.

**2.8. Evaluar un cultivo específico.** Busca el `id` del Mango en `GET /api/v1/cultivos`. Luego `POST /api/v1/predios/{predioId}/evaluaciones` con:
```json
{ "cultivoId": 15 }
```
(usa el id real del Mango). Esperado: **201**, `tipo` CULTIVO_ESPECIFICO y `resultadoCultivoConsultado` con compatibilidad **BAJA** y factores de altitud y temperatura LIMITANTES.

**2.9. Historial.** `GET /api/v1/predios/{predioId}/evaluaciones` → **200** con las 2 evaluaciones, la más reciente primero.

**2.10. Marcar alerta como leída.** `PATCH /api/v1/alertas/{id}/leida` con el id del paso 2.6 → **204**. Repite 2.6: ahora `noLeidas` es 0.

**2.11. Seguridad.** Todavía como José, `GET /api/v1/admin/cultivos` → **403** "No tiene permisos para realizar esta acción".

**2.12. Registrar un predio con clasificación de suelo.** Con los endpoints de **Ubigeo** busca los ids de **CUSCO** → **LA CONVENCION** → **SANTA ANA**. Luego `POST /api/v1/predios`:
```json
{
  "nombre": "Finca Quillabamba",
  "distritoId": 0,
  "latitud": -12.8525,
  "longitud": -72.7065,
  "areaHa": 3,
  "fuenteAgua": "RIEGO_GRAVEDAD"
}
```
(reemplaza el `0` por el id de Santa Ana). Esperado: **201**. Anota el `id` del nuevo predio.

**2.13. Suelo y evaluación de Finca Quillabamba.**
- `GET /api/v1/predios/{id}/suelo` → **200** con `"disponible": true`, `codigoCumOriginal` "C3s(r)", grupo **C** (cultivos permanentes), calidad **BAJA** y limitaciones **suelo**.
- `POST /api/v1/predios/{id}/evaluaciones` con `{}` → **201**. En el ranking deben aparecer cultivos permanentes de clima cálido (café, cacao, plátano, palta...). La papa y la quinua no aparecen: el factor SUELO es LIMITANTE (tierra C no admite cultivos en limpio) y la altitud y temperatura no corresponden.

Con los dos predios se ven los dos comportamientos del sistema: sin clasificación oficial (El Tambo) y con clasificación (Quillabamba).

---

## Parte 3. Perfil, administración de usuarios y reportes

**3.1. Editar mi perfil (como José).** `PUT /api/v1/usuarios/me` con el ejemplo de Swagger → **200** con los datos actualizados.

**3.2. Crear un usuario asesor.** `POST /api/v1/auth/register` (no necesita token):
```json
{
  "nombres": "Ana",
  "apellidos": "Torres",
  "email": "asesor@test.com",
  "password": "clave1234",
  "organizacion": "Agencia Agraria Huancayo",
  "tipoUsuario": "ASESOR"
}
```
Esperado: **201** con `"rol": "ASESOR"`.

**3.3. Seguridad de reportes.** Todavía con el token de José (PRODUCTOR): `GET /api/v1/reportes/indicadores` → **403**. Los reportes son solo para ASESOR y ADMIN.

**3.4. Administración de usuarios (como admin).** Logout → login con `admin@agrocrew.pe` / `Admin12345` → Authorize.
- `GET /api/v1/admin/usuarios` → **200** con los 3 usuarios. Anota el `id` de Ana.
- `PATCH /api/v1/admin/usuarios/{id}/desactivar` con el id de Ana → **200** y `"activo": false`. Si Ana intenta hacer login ahora, recibe **403** "La cuenta está desactivada".
- `PATCH /api/v1/admin/usuarios/{id}/activar` → **200** y `"activo": true`.

**3.5. Reportes (como asesor).** Logout → login con `asesor@test.com` / `clave1234` → Authorize. En la sección **Reportes**:

| Endpoint | Qué muestra |
|---|---|
| `GET /api/v1/reportes/indicadores` | Totales de la plataforma y % de evaluaciones completadas y preventivas |
| `GET /api/v1/reportes/predios-por-departamento` | Predios registrados por departamento (JUNIN: 1) |
| `GET /api/v1/reportes/evaluaciones-por-grupo-cum` | Evaluaciones por grupo de suelo (prueba también con `departamentoId` de Junín) |
| `GET /api/v1/reportes/cultivos-mas-recomendados` | Top 10 de cultivos recomendados con su puntaje promedio |
| `GET /api/v1/reportes/predios-por-nivel-riesgo` | Predios evaluados por departamento y nivel de riesgo |
| `GET /api/v1/reportes/puntos-criticos-por-departamento` | Puntos críticos de la ANA por departamento y nivel |
| `GET /api/v1/reportes/alertas-por-mes` (anio = 2026) | Alertas generadas por mes y tipo |

Todos deben responder **200**. Si alguna lista sale vacía es porque aún no hay datos de ese tipo (por ejemplo, si no se sincronizó la ANA).

---

## Parte 4. Pruebas unitarias

En IntelliJ, clic derecho sobre la carpeta `src/test/java` → **Run 'All Tests'**. Deben pasar todas (en verde). Toma captura del panel de resultados.
