# API REST

Base: `http://localhost:8080` (local) · documentación interactiva en `/swagger-ui.html` · contrato OpenAPI en `/v3/api-docs`.
Todas las respuestas son JSON (salvo el PDF). Las fechas usan ISO-8601 en UTC, por ejemplo `2026-11-22T12:00:00Z`.

## Pacientes

| Método | Ruta | Descripción | Código |
|---|---|---|---|
| POST | `/api/pacientes` | Registrar paciente | 201 / 400 / 409 |
| GET | `/api/pacientes` | Listar pacientes | 200 |
| GET | `/api/pacientes/{id}` | Consultar paciente | 200 / 404 |
| PUT | `/api/pacientes/{id}` | Actualizar paciente | 200 / 404 / 409 |
| PUT | `/api/pacientes/{id}/configuracion-mapa` | Tamaño de la cuadrícula (RF02) | 200 / 400 |
| DELETE | `/api/pacientes/{id}` | Eliminar paciente y todos sus datos | 204 |

```json
POST /api/pacientes
{
  "nombres": "Ana Maria", "apellidos": "Gomez Rojas", "documentoIdentidad": "1001234567",
  "fechaNacimiento": "1990-05-10", "tipoDiabetes": "TIPO_1",
  "email": "ana.gomez@example.com", "telefono": "3001234567",
  "configuracionMapa": { "filas": 3, "columnas": 4 },
  "cuidadores": [ { "nombre": "Luis Gomez", "parentesco": "Hermano" } ],
  "medicoTratante": { "nombre": "Dra. Laura Perez", "especialidad": "Endocrinologia" }
}
```

## Mapa corporal y recomendación

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/pacientes/{id}/mapa` | Mapa 2D completo: 4 zonas × 2 lados × matriz de celdas con semáforo, más el resumen y la recomendación |
| GET | `/api/pacientes/{id}/mapa/microzonas/{microzonaId}` | Estado de una microzona y de sus vecinas del grafo |
| GET | `/api/pacientes/{id}/recomendacion` | Zona sugerida, motivo, alternativas, zonas saturadas y olvidadas, `tiempoCalculoMs` |
| GET | `/api/pacientes/{id}/estadisticas` | Uso por zona (30 días), porcentaje de adherencia, registros con advertencia |
| GET | `/api/catalogo/zonas` | Zonas macro con prefijo y horas base de recuperación |

```json
GET /api/pacientes/{id}/recomendacion
{
  "microzona": { "id": "bra_der_01", "zonaMacro": "BRAZOS", "lado": "DERECHO", "fila": 0, "columna": 0,
                 "semaforo": "VERDE", "porcentajeRecuperacion": 100, "horasDescanso": 1440.0, ... },
  "puntaje": 2160.0,
  "motivo": "bra_der_01 (Brazos) acumula 1440.0 h de descanso y no registra aplicaciones recientes. Zona olvidada: se prioriza.",
  "hayZonasOptimas": true,
  "alternativas": [ ... ],
  "zonasSaturadas": ["ABDOMEN"],
  "zonasOlvidadas": ["MUSLOS", "BRAZOS", "GLUTEOS"],
  "tiempoCalculoMs": 1.27
}
```

## Aplicaciones (inyecciones)

| Método | Ruta | Descripción | Código |
|---|---|---|---|
| GET | `/api/pacientes/{id}/aplicaciones/validar/{microzonaId}` | Validación previa: semáforo y advertencia no bloqueante (RF04) | 200 / 404 |
| POST | `/api/pacientes/{id}/aplicaciones` | Registrar inyección (RF03, RF07) | 201 / 400 / 404 |
| POST | `/api/pacientes/{id}/aplicaciones/deshacer` | Deshacer el último registro (RF05) | 200 / 409 |
| GET | `/api/pacientes/{id}/aplicaciones?desde=&hasta=&zona=` | Historial (RF11) | 200 |
| GET | `/api/pacientes/{id}/aplicaciones/{aplicacionId}` | Consultar aplicación | 200 / 404 |
| GET | `/api/pacientes/{id}/aplicaciones/exportar/pdf?desde=&hasta=` | Reporte PDF para el médico (RF11) | 200 `application/pdf` |

```json
POST /api/pacientes/{id}/aplicaciones
{ "microzonaId": "abd_izq_04", "tipoInsulina": "RAPIDA", "unidades": 6 }

201 Created
{
  "aplicacion": { "id": "...", "microzonaId": "abd_izq_04", "semaforoAlRegistrar": "ROJO", ... },
  "advertencia": "Microzona bloqueada: aplicacion reciente. Recuperacion: 0%, disponible desde 25/11 11:28. ...",
  "microzonasBloqueadas": ["abd_izq_04", "abd_izq_03", "abd_izq_07", "abd_izq_08", "abd_der_01", "abd_der_05"],
  "deshaceresDisponibles": 2
}
```

Campos opcionales: `fechaHora` (si se omite, se usa la hora actual), `registradoPor` (`PACIENTE` por defecto o `CUIDADOR`) y `notas`.

## Cronogramas de dosis (RF10)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/pacientes/{id}/cronogramas` | Crear |
| GET | `/api/pacientes/{id}/cronogramas` | Listar |
| GET | `/api/pacientes/{id}/cronogramas/{cronogramaId}` | Consultar |
| PUT | `/api/pacientes/{id}/cronogramas/{cronogramaId}` | Actualizar |
| PATCH | `/api/pacientes/{id}/cronogramas/{cronogramaId}/estado?activo=false` | Activar o desactivar |
| DELETE | `/api/pacientes/{id}/cronogramas/{cronogramaId}` | Eliminar |

```json
{ "nombre": "Insulina basal", "tipoInsulina": "PROLONGADA", "unidades": 20,
  "horas": ["07:00", "21:00"], "diasSemana": ["MONDAY", "WEDNESDAY", "FRIDAY"] }
```

## Recordatorios (RF10)

| Método | Ruta | Descripción | Código |
|---|---|---|---|
| GET | `/api/pacientes/{id}/recordatorios?estado=ENVIADO` | Bandeja de notificaciones | 200 |
| GET | `/api/recordatorios/{id}` | Consultar | 200 / 404 |
| POST | `/api/recordatorios/{id}/confirmar` | Confirmar. Con `{"microzonaId": "..."}` registra también la inyección | 200 / 409 |
| POST | `/api/recordatorios/{id}/aplazar` | Aplazar (`{"minutos": 10}`, por defecto 15, máximo 3 veces) | 200 / 409 |
| POST | `/api/recordatorios/{id}/omitir` | Omitir la dosis | 200 / 409 |
| POST | `/api/recordatorios/procesar` | Ejecutar el ciclo del planificador manualmente | 200 |

## Formato de error

```json
{
  "timestamp": "2026-11-22T16:28:28Z",
  "estado": 400,
  "error": "Bad Request",
  "mensaje": "La peticion tiene campos invalidos",
  "ruta": "/api/pacientes",
  "detalles": ["documentoIdentidad: no debe estar vacío", "tipoDiabetes: no debe ser nulo"]
}
```

| Código | Cuándo |
|---|---|
| 400 | Validación, formato de fecha o enum inválido, fecha futura, cuadrícula mayor a 8 |
| 404 | Paciente, microzona, aplicación, cronograma o recordatorio inexistente |
| 409 | Documento duplicado, nada para deshacer, recordatorio ya respondido, máximo de aplazamientos |
