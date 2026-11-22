# Requerimientos

## 1. Requerimientos funcionales y trazabilidad

| ID | Requisito / regla de negocio | Implementación | Endpoint | Prueba |
|---|---|---|---|---|
| **RF01** | Mapear visualmente el cuerpo en 2D, dividido en 4 zonas macro: abdomen, muslos, brazos y glúteos. | `ZonaMacro`, `MapaCorporal` | `GET /api/pacientes/{id}/mapa`, `GET /api/catalogo/zonas` | `MapaCorporalTest` |
| **RF02** | Subdividir cada zona macro en microzonas con una cuadrícula de tamaño configurable. | `ConfiguracionMapa`, `CuadriculaZona` (matriz 2D) | `PUT /api/pacientes/{id}/configuracion-mapa` | `EstructurasDatosTest.cuadriculaGeneraIds` |
| **RF03** | Registrar una inyección tocando directamente la microzona del mapa. | `AplicacionService.registrar` | `POST /api/pacientes/{id}/aplicaciones` | Postman 3.x |
| **RF04** | Permitir el registro en una microzona no recuperada (roja o amarilla) mostrando una advertencia previa, sin bloquear. | `AplicacionService.validar` / `construirAdvertencia` | `GET .../aplicaciones/validar/{microzonaId}` | `AplicacionControllerTest` |
| **RF05** | Deshacer el último registro y restablecer el estado previo de la microzona. | `PilaDeshacer` (LIFO), `AplicacionService.deshacerUltima` | `POST .../aplicaciones/deshacer` | `EstructurasDatosTest.pilaLifo` |
| **RF06** | Semáforo de recuperación: Rojo (bloqueada), Amarillo (en recuperación), Verde (disponible). | `Semaforo`, `CalculadoraRecuperacion.semaforo` | `GET .../mapa` | `MotorRecomendacionTest.semaforoEnElTiempo` |
| **RF07** | Al registrar, bloquear la microzona usada y sus áreas adyacentes. | `GrafoAdyacencia`, `MotorRecomendacion.evaluar` (paso 2) | `POST .../aplicaciones` → `microzonasBloqueadas` | `MotorRecomendacionTest.bloqueoPorAdyacencia` |
| **RF08** | Calcular el tiempo de recuperación según la zona, el tiempo transcurrido y la frecuencia de uso. | `CalculadoraRecuperacion.horasRecuperacion` | `GET .../recomendacion` | `MotorRecomendacionTest.frecuenciaAlargaRecuperacion` |
| **RF09** | Penalizar las zonas macro saturadas (uso por encima del promedio en 30 días) y priorizar las olvidadas (15 días o más sin uso). | `ResumenZonaMacro`, `MonticuloMaximo` | `GET .../recomendacion`, `GET .../estadisticas` | `MotorRecomendacionTest.saturadaYOlvidada` |
| **RF10** | Configurar un cronograma de dosis, emitir recordatorios y gestionar su confirmación o aplazamiento. | `CronogramaService`, `RecordatorioService`, `ColaAlertas` (FIFO), `PlanificadorRecordatorios` | `/cronogramas`, `/recordatorios/{id}/confirmar\|aplazar\|omitir` | `EstructurasDatosTest.colaFifo`, Postman 4.x y 5.x |
| **RF11** | Guardar el historial completo y exportarlo (PDF) para el seguimiento médico. | `AplicacionService.historial`, `ReporteService` (PDFBox) | `GET .../aplicaciones`, `GET .../aplicaciones/exportar/pdf` | Postman 3.x |

## 2. Requerimientos no funcionales

| Atributo | Requisito | Cómo se cumple |
|---|---|---|
| **Usabilidad y accesibilidad** | Interfaz limpia, de baja carga cognitiva, con pocos toques. | La API permite el registro en un solo toque (solo 3 campos obligatorios). El mapa llega listo para pintar (`celdas[fila][columna]` con el semáforo). Los mensajes de advertencia y de error están en español. |
| **Rendimiento y latencia** | Mapa y cálculo de la zona óptima en < 200 ms. | El motor trabaja en memoria con estructuras O(1) y O(log n). La respuesta incluye `tiempoCalculoMs` (≈ 1–6 ms). La prueba `MotorRecomendacionTest.rendimiento` lo verifica con 512 microzonas y 1.000 registros. |
| **Confiabilidad e integridad de datos** | Tipado estricto al manipular registros médicos. | Java con tipado estático, `record` inmutables, `enum` para los valores cerrados, Bean Validation en todos los DTO, `Instant` (UTC) para las fechas, índice único en `documentoIdentidad` y en (`cronogramaId`, `fechaOriginal`). |
| **Escalabilidad modular** | Separación estricta entre interfaz, lógica y motor. | Paquetes `controlador` → `servicio` → `motor`. El motor no depende de la base de datos (ver [Vista de desarrollo](Vista-de-desarrollo)). |
| **Privacidad** | Datos sensibles y exportación controlada por el usuario. | Solo el usuario solicita la exportación. `DELETE /api/pacientes/{id}` borra en cascada. Los parámetros clínicos son configurables y no están quemados en el código. |
| **Mantenibilidad** | Código probado. | 24 pruebas automáticas (JUnit 5, AssertJ, MockMvc) y 39 peticiones de Postman con aserciones. |

## 3. Reglas de negocio parametrizables

Todos los valores clínicos están en `application.properties` (`insumap.motor.*`):

| Parámetro | Valor | Regla |
|---|---|---|
| `horas-base-abdomen` / `muslos` / `brazos` / `gluteos` | 72 / 96 / 96 / 120 h | Tiempo base de recuperación por zona (RF08) |
| `factor-frecuencia` | 0,25 | +25 % de recuperación por cada uso extra de la microzona en 30 días |
| `factor-frecuencia-maximo` | 2,0 | Tope del multiplicador por frecuencia |
| `factor-adyacencia` | 0,5 | Las vecinas se bloquean el 50 % del tiempo (RF07) |
| `umbral-rojo` | 0,5 | Progreso < 50 % = ROJO, < 100 % = AMARILLO, ≥ 100 % = VERDE (RF06) |
| `dias-ventana` | 30 | Ventana de análisis de saturación (RF09) |
| `dias-zona-olvidada` | 15 | Días sin uso para considerar una zona olvidada (RF09) |
| `penalizacion-zona-saturada` | 0,6 | Multiplica el puntaje de las zonas saturadas |
| `bonificacion-zona-olvidada` | 1,5 | Multiplica el puntaje de las zonas olvidadas |
| `insumap.mapa.filas-por-defecto` × `columnas-por-defecto` | 3 × 4 | Cuadrícula por defecto (96 microzonas) |
| `insumap.recordatorios.minutos-aplazamiento` / `maximo-aplazamientos` | 15 / 3 | Aplazamiento de recordatorios (RF10) |
