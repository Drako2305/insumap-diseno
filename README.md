# Insumap – Backend

Sistema inteligente de mapeo corporal y gestión de rotación para la administración de insulina.
API REST en **Java 21 + Spring Boot 4.2** con base de datos **MongoDB**.

| | |
|---|---|
| **API desplegada** | https://insumap-diseno.onrender.com |
| **Swagger UI** | https://insumap-diseno.onrender.com/swagger-ui.html |
| **Estado del servicio** | https://insumap-diseno.onrender.com/actuator/health |
| **Documentación y diagramas UML** | [Wiki del proyecto](https://github.com/Drako2305/insumap-diseno/wiki) |

> El servicio usa el plan gratuito de Render: tras 15 minutos sin uso se suspende y la primera
> petición tarda unos 50 segundos en responder.

## Tecnologías

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.2 (Web MVC, Data MongoDB, Validation, Actuator) |
| Base de datos | MongoDB 9 (local) · MongoDB Atlas (producción) |
| Documentación de la API | springdoc-openapi (Swagger UI) |
| Reportes PDF | Apache PDFBox |
| Pruebas | JUnit 5, Mockito, AssertJ, MockMvc · Postman |
| Despliegue | Docker · Render · MongoDB Atlas |

## Estructura del repositorio

```
insumap-diseno/
├── src/main/java/co/edu/ucc/insumap/
│   ├── controlador/      API REST (capa de presentación)
│   ├── servicio/         Lógica de negocio
│   ├── motor/            Motor de recomendación heurística
│   │   └── estructuras/  Matriz 2D, tabla hash, grafo, Max-Heap, pila y cola
│   ├── modelo/           Documentos de MongoDB y enumeraciones
│   ├── repositorio/      Acceso a datos (Spring Data MongoDB)
│   ├── dto/              Objetos de petición y respuesta
│   ├── config/           Configuración y parámetros del motor
│   └── excepcion/        Manejo uniforme de errores
├── src/main/resources/application.properties
├── src/test/java/        Pruebas automáticas
├── postman/              Colección y entornos de Postman
├── Dockerfile            Imagen de producción
├── render.yaml           Despliegue en Render
└── pom.xml
```

## Funcionalidades (RF01 – RF11)

- Mapa corporal 2D con 4 zonas macro (abdomen, muslos, brazos y glúteos), subdivididas en microzonas con una cuadrícula configurable.
- Semáforo de recuperación por microzona: rojo, amarillo y verde.
- Registro de inyecciones con advertencia no bloqueante y bloqueo de las microzonas adyacentes.
- Deshacer el último registro.
- Recomendación de la zona óptima según el tiempo de recuperación, la frecuencia de uso, las zonas saturadas y las zonas olvidadas.
- Cronograma de dosis con recordatorios: confirmar, aplazar u omitir.
- Historial completo y exportación en PDF para el médico tratante.

## Ejecutar en local

Requisitos: **Java 21** y **MongoDB** en `localhost:27017`.

```bash
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

- API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui.html

Para usar otra base de datos, define la variable de entorno `MONGODB_URI`.

## Pruebas

```bash
./mvnw test
```

**Postman:** importa los archivos de la carpeta `postman/` y elige el entorno
*Insumap - Local* o *Insumap - Produccion*. Luego ejecuta la colección con el *Collection Runner*.
