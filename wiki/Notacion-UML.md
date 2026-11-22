> Página generada desde `diagramas/README.md` del repositorio (https://github.com/Drako2305/insumap-diseno/blob/main/diagramas/README.md).

# Diagramas UML de Insumap

Los diagramas siguen la plantilla del curso (*Modelo de presentación de diseño*): cinco vistas de arquitectura.
Cada archivo trae dos partes:

1. **El diagrama en Mermaid.** GitHub lo dibuja directamente, en el repositorio y en la wiki. Sirve como referencia.
2. **La guía de construcción en Visual Paradigm.** Lista exacta de elementos, tipos, multiplicidades y tipo de flecha para dibujar el diagrama oficial.

| # | Vista | Archivo | Diagramas UML |
|---|---|---|---|
| 1 | Vista de contexto | [01-vista-contexto.md](Vista-de-contexto) | Diagrama de contexto (sistema, actores y sistemas externos) |
| 2 | Vista funcional | [02-vista-funcional.md](Vista-funcional) | Casos de uso + especificación + actividad "Registrar inyección" |
| 3 | Vista lógica | [03-vista-logica.md](Vista-logica) | Clases (dominio, motor, servicios), secuencia y máquinas de estado |
| 4 | Vista de desarrollo | [04-vista-desarrollo.md](Vista-de-desarrollo) | Paquetes y componentes |
| 5 | Vista de despliegue | [05-vista-despliegue.md](Vista-de-despliegue) | Despliegue (nodos, artefactos, rutas de comunicación) |

Cuando exportes los diagramas desde Visual Paradigm (*File → Export → Image*), guárdalos en
[`imagenes/`](https://github.com/Drako2305/insumap-diseno/tree/main/diagramas/imagenes) con el nombre que indica cada guía.

## Notación UML usada (leyenda de flechas)

| Relación | Significado en Insumap | Flecha UML | En Visual Paradigm | En Mermaid |
|---|---|---|---|---|
| **Asociación** | Una clase guarda una referencia a otra (ej. `Aplicacion` → `ZonaMacro`) | Línea continua con punta abierta `——>` | *Association* (navegable) | `-->` |
| **Agregación** | Todo–parte en el que la parte puede existir sola | Rombo **vacío** en el lado del todo `◇——` | *Aggregation* | `o--` |
| **Composición** | Todo–parte en el que la parte muere con el todo (documentos embebidos, matriz de celdas) | Rombo **relleno** en el lado del todo `◆——` | *Composition* | `*--` |
| **Dependencia** | Una clase usa a otra temporalmente (parámetro, variable local o creación) | Línea **punteada** con punta abierta `- - ->` | *Dependency* | `..>` |
| **Generalización** | Herencia o especialización (interfaz que extiende otra, actor que especializa a otro) | Línea continua con triángulo **vacío** `——▷` | *Generalization* | `--|>` |
| **Realización** | Una clase implementa una interfaz | Línea **punteada** con triángulo vacío `- - -▷` | *Realization* | `..|>` |
| **«include»** | Caso de uso obligatorio dentro de otro | Punteada con estereotipo, del base al incluido | *Include* | `-.->` |
| **«extend»** | Caso de uso opcional que extiende a otro | Punteada con estereotipo, del que extiende al base | *Extend* | `-.->` |

### Multiplicidades

| Multiplicidad | Lectura |
|---|---|
| `1` | Exactamente uno |
| `0..1` | Cero o uno (opcional) |
| `0..*` | Cero o muchos |
| `1..*` | Uno o muchos |
| `0..5` | Entre cero y cinco (máximo de cuidadores validado en la API) |
| `8` | Exactamente ocho (4 zonas × 2 lados) |

### Tipos de datos en los atributos

Se usan los tipos reales de Java del código. En Visual Paradigm se escriben en el campo *Type* de cada atributo.

| Tipo | Uso | Ejemplo |
|---|---|---|
| `int` | Enteros | `filas : int`, `aplazamientos : int` |
| `double` | Decimales (equivale a `float` con doble precisión) | `unidades : double`, `puntaje : double` |
| `boolean` | Verdadero/falso | `deshecha : boolean`, `activo : boolean` |
| `String` | Texto e identificadores de MongoDB (ObjectId) | `id : String`, `microzonaId : String` |
| `LocalDate` | Fecha sin hora | `fechaNacimiento : LocalDate` |
| `Instant` | Fecha y hora en UTC | `fechaHora : Instant` |
| `List<T>`, `Set<T>`, `Map<K,V>` | Colecciones | `cuidadores : List<Cuidador>` |

Visibilidad: `-` privado, `+` público, `#` protegido. Los atributos son privados y los métodos públicos.
Los atributos estáticos van subrayados.
