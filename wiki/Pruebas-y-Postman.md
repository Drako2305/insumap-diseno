# Pruebas y Postman

## Pruebas automáticas (`./mvnw test`)

| Clase de prueba | Pruebas | Qué verifica |
|---|---|---|
| `EstructurasDatosTest` | 8 | Matriz 2D, grafo no dirigido, Max-Heap (200 valores aleatorios en orden descendente), pila LIFO con capacidad, cola FIFO |
| `MapaCorporalTest` | 5 | 96 microzonas, vecindad de 8 direcciones, línea media del abdomen, sin adyacencia entre zonas |
| `MotorRecomendacionTest` | 7 | Semáforo en el tiempo (RF06), bloqueo de adyacentes (RF07), frecuencia (RF08), saturadas y olvidadas (RF09), aplicaciones deshechas, **rendimiento < 200 ms** |
| `AplicacionControllerTest` | 4 | 201 con advertencia no bloqueante, 400 por validación, 409 al deshacer sin registros, 400 por enum inválido |
| **Total** | **24** | Todas pasan |

## Colección de Postman

Archivos en la carpeta [`postman/`](https://github.com/Drako2305/insumap-diseno/tree/main/postman) del repositorio:

| Archivo | Uso |
|---|---|
| `Insumap.postman_collection.json` | Colección de 39 peticiones con aserciones |
| `Insumap-Local.postman_environment.json` | `baseUrl = http://localhost:8080` |
| `Insumap-Produccion.postman_environment.json` | `baseUrl` del despliegue en Render |
| `generar_coleccion.py` | Script que regenera la colección |

### Cómo usarla

1. Postman → **Import** → arrastra los tres archivos JSON.
2. Arriba a la derecha, selecciona el entorno **Insumap - Local**.
3. Clic derecho en la colección → **Run collection** → **Run Insumap API**.
4. Las variables `pacienteId`, `aplicacionId`, `cronogramaId`, `recordatorioId` y `microzonaSugerida` se llenan solas.
   El documento del paciente se genera al azar en cada ejecución, así que se puede correr varias veces.

### Carpetas

| Carpeta | Peticiones | Requisitos |
|---|---|---|
| 0. Salud y catálogo | 2 | RF01, RF08 |
| 1. Pacientes | 9 (incluye 409, 400 y 404) | RF02 |
| 2. Mapa corporal y recomendación | 4 | RF01, RF06–RF09 |
| 3. Aplicaciones | 12 (incluye advertencia en ROJO, deshacer, fecha futura, PDF) | RF03–RF05, RF07, RF11 |
| 4. Cronogramas | 4 | RF10 |
| 5. Recordatorios | 6 (procesar, aplazar, confirmar con inyección, 409) | RF10 |
| 6. Limpieza | 2 | – |

Resultado de la ejecución contra el backend local: **39 de 39 peticiones con el código esperado**.

> Para guardar el PDF desde Postman, usa la flecha del botón *Send* → **Send and Download**.
