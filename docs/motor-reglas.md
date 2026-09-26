# Motor de reglas de compatibilidad (TS06 / T25)

El ranking de cultivos se calcula con reglas fijas en `MotorReglasService`. No usa IA: con los mismos datos del predio, el resultado es siempre el mismo. La IA (Parte 2, TS07) solo redactará la explicación.

## Factores y puntaje (0 a 100)

| Factor | Peso | FAVORABLE (100 % del peso) | NEUTRO (50 %) | LIMITANTE (0 %) |
|---|---|---|---|---|
| Suelo (grupo CUM) | 30 | La tierra admite el cultivo | Sin clasificación oficial | La tierra no admite el cultivo, o es de protección (X) |
| Altitud | 20 | Dentro del rango | Fuera por ≤ 300 m, o sin dato | Fuera por > 300 m |
| Temperatura media | 20 | Dentro del rango | Fuera por ≤ 2 °C, o sin dato | Fuera por > 2 °C |
| Agua (lluvia o riego) | 20 | Lluvia en rango, o falta lluvia pero hay riego | Hasta 30 % menos o más de lluvia, o sin dato | Déficit > 30 % en secano, cultivo que requiere riego en secano, o exceso > 30 % |
| Riesgo hídrico | 10 | Riesgo bajo (o medio con tolerancia alta) | Riesgo tolerado parcialmente | Tolerancia baja con riesgo alto/muy alto, o tolerancia media con riesgo muy alto |

**Regla del suelo:** los grupos CUM tienen un orden A > C > P > F > X. Una tierra admite los usos de su grupo y de los grupos inferiores (una tierra A admite cultivos en limpio, permanentes, pastos y forestales; una tierra C no admite cultivos en limpio). Las tierras X no admiten ningún cultivo.

## Compatibilidad

- **ALTA:** puntaje ≥ 75
- **MEDIA:** puntaje ≥ 50
- **BAJA:** puntaje < 50

Topes: si el suelo, la altitud o la temperatura son LIMITANTES, el puntaje máximo es 49 (BAJA). Si el agua o el riesgo son LIMITANTES, el máximo es 74 (MEDIA).

## Ranking

Se ordena de mayor a menor puntaje (en empate, por nombre) y se muestran hasta 10 cultivos con compatibilidad ALTA o MEDIA. Si el productor consultó un cultivo específico, su resultado se muestra aparte aunque sea BAJA.

## Fuentes de datos

- Suelo: MIDAGRI, Clasificación de Tierras por su Capacidad de Uso Mayor.
- Altitud: indicada por el productor o la de la capital del distrito (INEI).
- Temperatura y lluvia: Open-Meteo (reanálisis ERA5), últimos 12 meses.
- Riesgo hídrico: Puntos Críticos de la ANA a menos de 5 km.
- Requerimientos de cada cultivo: catálogo propio (`resources/data/cultivos.csv`), valores referenciales por validar con fuentes oficiales (T14).

## Nivel de riesgo de los puntos críticos de la ANA

La capa de Puntos Críticos de la ANA identifica lugares con probabilidad de daño por inundación o desborde, pero no publica un nivel de riesgo. Criterio del proyecto:

- Todo punto crítico se considera riesgo **ALTO**.
- Se considera **MUY ALTO** si la ANA registra 50 o más familias o viviendas expuestas, o algún centro educativo o de salud expuesto.
- Un predio se evalúa con el mayor nivel entre los puntos críticos que tenga a menos de 5 km (`app.riesgo.radio-km`). Sin puntos cercanos, el riesgo es BAJO.
