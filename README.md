# Insumap – Backend (Diseño de Software)

Sistema inteligente de mapeo corporal y gestión de rotación para la administración de insulina.
Backend REST en **Java 21 + Spring Boot 4.2** con base de datos **MongoDB**.

> Proyecto académico. No es un dispositivo médico y no reemplaza el criterio clínico.

## Estructura del repositorio

| Carpeta | Contenido |
|---|---|
| `src/main/java/co/edu/ucc/insumap` | Código fuente del backend |
| `src/test/java` | Pruebas unitarias y de controlador |
| `postman/` | Colección y entornos de Postman |
| `diagramas/` | Diagramas UML (fuente PlantUML, imágenes y guía para Visual Paradigm) |
| `wiki/` | Páginas de la wiki de GitHub (caso de estudio y vistas de arquitectura) |
| `despliegue/` | Guía de despliegue (Render + MongoDB Atlas) |

## Paquetes del código

| Paquete | Responsabilidad |
|---|---|
| `controlador` | API REST (capa de presentación) |
| `servicio` | Lógica de negocio y casos de uso |
| `motor` | Motor de recomendación heurística (independiente de la base de datos) |
| `motor.estructuras` | Matriz 2D, tabla hash, grafo no dirigido, Max-Heap, pila LIFO y cola FIFO |
| `modelo` | Documentos de MongoDB y enumeraciones |
| `repositorio` | Acceso a datos con Spring Data MongoDB |
| `dto` | Objetos de petición y respuesta |
| `config` | Configuración y parámetros del motor |
| `excepcion` | Excepciones de dominio y manejo uniforme de errores |

## Cómo ejecutar en local

Requisitos: Java 21 y MongoDB en `localhost:27017`.

```bash
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

- API: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Salud: `http://localhost:8080/actuator/health`

Para usar otra base de datos: variable de entorno `MONGODB_URI`.

## Pruebas

```bash
./mvnw test
```
