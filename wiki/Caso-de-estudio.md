# Caso de estudio: Insumap

> Sistema Inteligente de Mapeo Corporal y Gestión de Rotación para la Administración de Insulina

## 1. Contexto y problemática del dominio

Los pacientes con **diabetes mellitus** (tipo 1 y tipo 2 insulinodependientes) se aplican varias dosis
subcutáneas de insulina al día para regular la glucosa en sangre. Para cuidar el tejido y absorber bien
el fármaco, los protocolos clínicos exigen **rotar estrictamente** entre zonas anatómicas: **abdomen, muslos,
brazos y glúteos**.

En la práctica, cumplir ese protocolo choca con barreras humanas y operativas:

| Barrera | Descripción |
|---|---|
| **Carga cognitiva** | Un paciente promedio se hace más de **1.000 punciones al año**. Recordar de memoria los sitios usados recientemente es insostenible. |
| **Sesgo de comodidad** | Los brazos y los glúteos son difíciles de alcanzar o de ver, así que los usuarios repiten las inyecciones en el **abdomen frontal**. |
| **Ausencia de herramientas dinámicas** | Los registros en papel o libretas son tediosos, no calculan tiempos biológicos de cicatrización y terminan abandonados. |

### Impacto clínico

Puncionar repetidamente la misma área provoca **lipodistrofia**: tejido graso endurecido o hundimientos de la piel.
La insulina inyectada sobre tejido lesionado se absorbe de forma **errática e impredecible**. Esto causa
descompensaciones graves (hiperglucemia o hipoglucemia severa), ajustes erróneos de la dosis y más costos por
urgencias.

## 2. Propuesta de solución

**Insumap** es una aplicación del sector salud (*mHealth*) que reemplaza la memoria humana y los métodos
manuales por un sistema visual asistido:

- Un **mapa corporal interactivo en 2D**, dividido en cuadrículas de **microzonas**.
- Un **motor de recomendación algorítmica** que:
  - revisa el historial de aplicaciones,
  - lleva la cuenta del **tiempo biológico de recuperación** de cada punto de la piel,
  - **bloquea** las áreas usadas hace poco y las **adyacentes**,
  - **sugiere** por sí solo la ubicación óptima para la siguiente dosis.

## 3. Actores e interesados

| Actor | Rol | Interacción con el sistema |
|---|---|---|
| **Paciente insulinodependiente** | Usuario principal | Usa el mapa a diario para ver la zona sugerida, registrar dosis, revisar el historial y gestionar alertas. |
| **Cuidador / Familiar** | Usuario de apoyo | Ayuda al paciente, sobre todo a adultos mayores y niños, a registrar inyecciones en zonas de difícil acceso. |
| **Médico tratante / Endocrinólogo** | Actor secundario | Recibe los reportes históricos exportados para evaluar la adherencia a la rotación y ajustar el tratamiento. |

## 4. Retos de diseño

| Reto | Decisión de diseño | Dónde se implementa |
|---|---|---|
| **Automatización vs. autonomía** | El sistema sugiere, pero **no impide** registrar en otra zona: muestra advertencias **no bloqueantes** (RF04). | `AplicacionService.validar` y `registrar` |
| **Desincronización físico-digital** | Registro en **un solo toque**: basta con `microzonaId`, `tipoInsulina` y `unidades`. La hora se pone sola. | `RegistrarAplicacionRequest.fechaHora` opcional |
| **Privacidad y normativa** | Los datos de salud son sensibles: la exportación la controla el usuario y el paciente puede eliminar todos sus datos. | `GET .../exportar/pdf`, `DELETE /api/pacientes/{id}` |

## 5. Decisiones de esta implementación

El caso original propone TypeScript. Para la entrega de la materia se pidió **Java + Spring Boot y MongoDB**,
manteniendo la misma arquitectura en tres capas:

| Capa del caso | Implementación |
|---|---|
| Presentación / Frontend | API REST (`controlador`). El cliente se prueba con **Postman** y Swagger UI. |
| Lógica de negocio / Backend | `servicio` |
| Microservicio de recomendación heurística | `motor` y `motor.estructuras`, sin dependencias de base de datos ni de Spring Web |

El **tipado estricto** que pide el atributo de confiabilidad se cumple con Java (tipado estático),
*records* inmutables, `enum` para todos los valores cerrados y validación declarativa en los DTO.

Siguiente: [Requerimientos](Requerimientos)
