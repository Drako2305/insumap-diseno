# Insumap - Mapeo Corporal y Rotación de Insulina (Backend)

![Java](https://img.shields.io/badge/Java%2021-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-4EA94B?style=for-the-badge&logo=mongodb&logoColor=white)
![Postman](https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white)

Backend de **Insumap**, un sistema para pacientes insulinodependientes que reemplaza la memoria y las libretas por
un mapa corporal 2D asistido: lleva el historial de inyecciones, calcula el tiempo de recuperación de cada punto de
la piel, bloquea las zonas recientes y sus vecinas, y sugiere la ubicación óptima para la siguiente dosis.
El caso de estudio, los requisitos y los diagramas están en la [wiki del repositorio](https://github.com/Drako2305/insumap-diseno/wiki).

| | |
|---|---|
| **API desplegada** | https://insumap-diseno.onrender.com |
| **Swagger UI** | https://insumap-diseno.onrender.com/swagger-ui.html |
| **Estado del servicio** | https://insumap-diseno.onrender.com/actuator/health |

> El servicio usa el plan gratuito de Render: tras 15 minutos sin uso se suspende y la primera petición tarda unos
> 50 segundos en responder.

## Alcance

Este entregable corresponde **únicamente al backend** (API REST). No incluye interfaz gráfica: toda la interacción
se hace y se verifica a través de los endpoints REST, documentados con Swagger UI y probados con la colección de
Postman incluida en el repositorio.

## Stack

- Java 21 (LTS)
- Spring Boot 4.2 (Spring Web MVC, Spring Data MongoDB, Validation, Actuator)
- MongoDB 9 en local y MongoDB Atlas en producción
- Swagger UI (springdoc) para documentar y probar la API
- Apache PDFBox para el reporte PDF del historial
- JUnit 5, Mockito, AssertJ y MockMvc para las pruebas
- Maven (con Maven Wrapper) y Docker para el despliegue en Render

## Estructura del repositorio

```
insumap-diseno
├── src/main/java/co/edu/ucc/insumap
│   ├── controlador   -> endpoints REST (pacientes, mapa, aplicaciones, cronogramas, recordatorios, catalogo)
│   ├── servicio      -> logica de negocio y planificador de recordatorios
│   ├── motor         -> motor de recomendacion heuristica
│   │   └── estructuras -> matriz 2D, tabla hash, grafo, Max-Heap, pila LIFO y cola FIFO
│   ├── repositorio   -> acceso a datos (MongoRepository)
│   ├── dto           -> objetos de entrada y salida
│   ├── modelo        -> documentos de MongoDB + modelo.enums
│   ├── excepcion     -> excepciones de negocio + manejador global
│   └── config        -> parametros del motor, Swagger y configuracion general
├── src/main/resources/application.properties
├── src/test/java     -> pruebas del motor, las estructuras y los controladores
├── postman           -> coleccion y entornos (local y produccion)
├── mongo             -> datos de ejemplo e instrucciones para cargarlos
├── Dockerfile        -> imagen de produccion
└── render.yaml       -> despliegue en Render
```

## Módulos

- **Pacientes:** registro del paciente, sus cuidadores, su médico tratante y el tamaño de la cuadrícula del mapa.
- **Mapa corporal y recomendación:** mapa 2D con semáforo por microzona y zona sugerida para la siguiente dosis.
- **Aplicaciones:** registro de inyecciones con advertencia no bloqueante, deshacer el último registro e historial.
- **Cronogramas y recordatorios:** dosis programadas y recordatorios que se confirman, aplazan u omiten.
- **Reportes:** exportación del historial en PDF para el médico tratante.

## Cómo ejecutar

1. Tener **Java 21** y **MongoDB** en `localhost:27017`. Para usar MongoDB Atlas, define la variable de entorno
   `MONGODB_URI` con la cadena `mongodb+srv://...`.
2. (Opcional) Cargar los datos de ejemplo de la carpeta [`mongo/`](mongo).
3. Ejecutar:

   ```
   ./mvnw spring-boot:run          (Windows: mvnw.cmd spring-boot:run)
   ```

4. Abrir la documentación interactiva en `http://localhost:8080/swagger-ui.html`.

Pruebas automáticas: `./mvnw test`.

## Pruebas con Postman

1. Importa los tres archivos de la carpeta [`postman/`](postman).
2. Elige el entorno **Insumap - Local** o **Insumap - Produccion**.
3. Ejecuta las carpetas en orden o toda la colección con **Run collection**. Los identificadores (`pacienteId`,
   `aplicacionId`, `recordatorioId`) se guardan solos entre peticiones.

## Endpoints principales

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/pacientes` | Registrar paciente |
| GET | `/api/pacientes` y `/api/pacientes/{id}` | Consultar pacientes |
| PUT | `/api/pacientes/{id}/configuracion-mapa` | Configurar la cuadrícula de microzonas (RF02) |
| GET | `/api/pacientes/{id}/mapa` | Mapa corporal 2D con semáforo (RF01, RF06) |
| GET | `/api/pacientes/{id}/recomendacion` | Zona sugerida para la siguiente dosis (RF08, RF09) |
| GET | `/api/pacientes/{id}/aplicaciones/validar/{microzonaId}` | Advertencia previa sin bloquear (RF04) |
| POST | `/api/pacientes/{id}/aplicaciones` | Registrar inyección (RF03, RF07) |
| POST | `/api/pacientes/{id}/aplicaciones/deshacer` | Deshacer el último registro (RF05) |
| GET | `/api/pacientes/{id}/aplicaciones` | Historial de aplicaciones (RF11) |
| GET | `/api/pacientes/{id}/aplicaciones/exportar/pdf` | Exportar el historial en PDF (RF11) |
| POST | `/api/pacientes/{id}/cronogramas` | Crear cronograma de dosis (RF10) |
| POST | `/api/recordatorios/{id}/confirmar` | Confirmar dosis (RF10) |
| POST | `/api/recordatorios/{id}/aplazar` | Aplazar recordatorio (RF10) |
| GET | `/api/pacientes/{id}/estadisticas` | Uso por zona y adherencia a la rotación |

Los errores responden con el código HTTP correcto (400, 404, 409) y un mensaje en español.
