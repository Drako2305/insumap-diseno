# 5. Vista de despliegue

Describe **dónde se ejecuta** cada pieza: nodos físicos o virtuales, entornos de ejecución, artefactos
desplegados y rutas de comunicación con sus protocolos.

## 5.1 Diagrama de despliegue – Producción

```mermaid
flowchart LR
    subgraph dispositivo["«device» Dispositivo del usuario<br/>(PC / smartphone)"]
        postman["«artifact»<br/>Postman /<br/>navegador web móvil"]
    end

    subgraph github["«node» GitHub"]
        repo["«artifact»<br/>Repositorio insumap-diseno<br/>(rama main)"]
    end

    subgraph render["«node» Render Cloud (Oregon) · plan Free"]
        subgraph docker["«execution environment» Contenedor Docker<br/>eclipse-temurin:21-jre · 512 MB"]
            subgraph jvm["«execution environment» JVM 21"]
                jar["«artifact»<br/>insumap.jar<br/>(Spring Boot 4.2 + Tomcat :PORT)"]
            end
        end
    end

    subgraph atlas["«node» MongoDB Atlas · cluster M0"]
        subgraph mongod["«execution environment» MongoDB Server"]
            db[("«database» insumap<br/>pacientes · aplicaciones ·<br/>cronogramas · recordatorios")]
        end
    end

    postman -- "HTTPS 443 · REST/JSON<br/>0..* ↔ 1" --> jar
    jar -- "TLS · mongodb+srv 27017<br/>1 ↔ 1" --> db
    repo -- "webhook push → docker build<br/>(autoDeploy)" --> docker
```

## 5.2 Diagrama de despliegue – Desarrollo local

```mermaid
flowchart LR
    subgraph pc["«device» PC del desarrollador · Windows 11"]
        subgraph jdk["«execution environment» OpenJDK 21"]
            jarLocal["«artifact» insumap.jar<br/>(mvnw spring-boot:run) :8080"]
        end
        subgraph servicio["«execution environment» Servicio Windows MongoDB 9.0"]
            dbLocal[("«database» insumap<br/>localhost:27017")]
        end
        postmanLocal["«artifact» Postman"]
    end
    postmanLocal -- "HTTP 8080" --> jarLocal
    jarLocal -- "mongodb://localhost:27017" --> dbLocal
```

## 5.3 Especificación de nodos y artefactos

| Nodo / entorno | Tipo UML | Especificación | Artefactos |
|---|---|---|---|
| Dispositivo del usuario | `«device»` | PC o smartphone con navegador o Postman | Colección `Insumap.postman_collection.json` |
| Render Cloud | `«node»` | Web Service Docker, plan Free, 512 MB RAM, HTTPS gestionado | – |
| Contenedor Docker | `«execution environment»` | Imagen multi-etapa `eclipse-temurin:21-jre`, usuario sin privilegios | `Dockerfile` |
| JVM 21 | `«execution environment»` | `-XX:MaxRAMPercentage=75 -XX:+UseSerialGC` | `insumap.jar` |
| MongoDB Atlas | `«node»` | Cluster M0 compartido, 512 MB, réplica de 3 nodos, TLS obligatorio | Base de datos `insumap` |
| GitHub | `«node»` | Repositorio público y disparador del despliegue | Código fuente, `render.yaml` |

| Ruta de comunicación | Protocolo | Multiplicidad |
|---|---|---|
| Dispositivo → insumap.jar | HTTPS (443), REST/JSON | `0..*` clientes a `1` servicio |
| insumap.jar → MongoDB Atlas | MongoDB Wire Protocol sobre TLS (`mongodb+srv`) | `1` a `1` |
| GitHub → Render | Webhook HTTPS (build automático) | `1` a `1` |

## Guía para Visual Paradigm

*Diagram → New → Deployment Diagram.* Nombre sugerido: `05-despliegue.png`.

1. Herramienta **Node** para `Dispositivo del usuario` (estereotipo `«device»`), `Render Cloud`, `MongoDB Atlas` y `GitHub`.
2. **Dentro** de `Render Cloud`, un **Node** con el estereotipo `«execution environment»` llamado `Contenedor Docker`. Dentro de él, otro `«execution environment»` llamado `JVM 21`.
3. Dentro de `JVM 21`, un **Artifact** `insumap.jar`. Dentro de `MongoDB Atlas`, un `«execution environment» MongoDB Server` con un **Artifact** `insumap` (estereotipo `«database»`). En el dispositivo, un **Artifact** `Postman`.
4. Une los nodos con **Communication Path** (línea continua). Ponle el protocolo como estereotipo, por ejemplo `«HTTPS»` o `«TLS/mongodb+srv»`, y multiplicidad en los extremos:

| Desde | Hacia | Estereotipo | Multiplicidad |
|---|---|---|---|
| Dispositivo del usuario | Render Cloud | `«HTTPS»` | 0..* – 1 |
| Render Cloud | MongoDB Atlas | `«TLS mongodb+srv»` | 1 – 1 |
| GitHub | Render Cloud | `«webhook»` | 1 – 1 |

5. Opcional: un **Artifact** `Dockerfile` fuera de los nodos, con una **Dependency** `«manifest»` hacia `insumap.jar`.
6. Repite los pasos en un segundo diagrama para el entorno local (5.2).
