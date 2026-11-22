# Insumap

**Sistema Inteligente de Mapeo Corporal y Gestión de Rotación para la Administración de Insulina**

Backend REST desarrollado para la materia **Diseño de Software** con **Java 21 + Spring Boot 4.2** y **MongoDB**.

| | |
|---|---|
| Repositorio | [Drako2305/insumap-diseno](https://github.com/Drako2305/insumap-diseno) |
| Lenguaje / framework | Java 21 · Spring Boot 4.2 |
| Base de datos | MongoDB 9 (local) · MongoDB Atlas (producción) |
| Pruebas de API | Postman (39 peticiones) · Swagger UI |
| Despliegue | Docker · Render · MongoDB Atlas |

## Contenido de la wiki

### 1. Caso de estudio
- [Caso de estudio](Caso-de-estudio): contexto, problemática, propuesta y actores
- [Requerimientos](Requerimientos): funcionales (RF01–RF11), no funcionales y trazabilidad al código

### 2. Diseño (vistas de arquitectura)
- [Vista de contexto](Vista-de-contexto)
- [Vista funcional](Vista-funcional): casos de uso y actividad
- [Vista lógica](Vista-logica): clases, secuencia y estados
- [Vista de desarrollo](Vista-de-desarrollo): paquetes y componentes
- [Vista de despliegue](Vista-de-despliegue)
- [Notación UML y guía de Visual Paradigm](Notacion-UML)

### 3. Implementación
- [Motor de recomendación](Motor-de-recomendacion): algoritmo y estructuras de datos
- [Modelo de datos en MongoDB](Modelo-de-datos)
- [API REST](API-REST)
- [Pruebas y Postman](Pruebas-y-Postman)
- [Despliegue](Despliegue)

## Inicio rápido

```bash
git clone https://github.com/Drako2305/insumap-diseno.git
cd insumap-diseno
./mvnw spring-boot:run          # requiere MongoDB en localhost:27017
```

Swagger UI: <http://localhost:8080/swagger-ui.html>
