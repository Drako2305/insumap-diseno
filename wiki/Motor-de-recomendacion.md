# Motor de recomendación

Paquete `co.edu.ucc.insumap.motor`. Es un componente **puro**: no depende de MongoDB ni de Spring Web.
Recibe el mapa corporal, el historial y la hora actual, y devuelve el estado de cada microzona y la zona óptima.

## 1. Identificación de microzonas

Cada zona macro tiene una cuadrícula por lado (izquierdo y derecho) de `filas × columnas` (por defecto 3 × 4).
El identificador es `{zona}_{lado}_{índice}`, con el índice contado por filas desde 01:

```
abd_izq (3x4)                  abd_der (3x4)
┌────┬────┬────┬────┐ ombligo ┌────┬────┬────┬────┐
│ 01 │ 02 │ 03 │ 04 │   ║     │ 01 │ 02 │ 03 │ 04 │
├────┼────┼────┼────┤   ║     ├────┼────┼────┼────┤
│ 05 │ 06 │ 07 │ 08 │   ║     │ 05 │ 06 │ 07 │ 08 │
├────┼────┼────┼────┤   ║     ├────┼────┼────┼────┤
│ 09 │ 10 │ 11 │ 12 │   ║     │ 09 │ 10 │ 11 │ 12 │
└────┴────┴────┴────┘         └────┴────┴────┴────┘
```

Prefijos: `abd` abdomen, `mus` muslos, `bra` brazos, `glu` glúteos · `izq` / `der`.

## 2. Grafo de adyacencia (RF07)

- Cada microzona es un **nodo**. Se conectan los **8 vecinos**: horizontal, vertical y diagonal.
- En el **abdomen**, la última columna del lado izquierdo y la primera del derecho están sobre la línea media,
  así que también se conectan. Por ejemplo, `abd_izq_04` es vecina de `abd_der_01` y `abd_der_05`.
- Zonas distintas no se conectan entre sí.
- Con 3 × 4 hay 96 nodos.

## 3. Algoritmo `evaluar(mapa, historial, ahora)`

1. **Historial (tabla hash O(1)).** Por cada aplicación no deshecha de los últimos 60 días se guarda la
   última fecha de uso de cada microzona y de cada zona macro, y se cuentan los usos en la ventana de 30 días.
2. **Tiempo de recuperación (RF08)** de cada microzona:

   ```
   horasRequeridas = horasBase(zona) × min(2.0, 1 + 0.25 × max(0, usos30d − 1))
   ```

   | Zona | Horas base |
   |---|---|
   | Abdomen | 72 h |
   | Muslos | 96 h |
   | Brazos | 96 h |
   | Glúteos | 120 h |

3. **Bloqueo por adyacencia (RF07).** Por cada microzona usada se recorren sus vecinas en el grafo.
   Cada vecina queda bloqueada `horasRequeridas(vecina) × 0.5` desde esa aplicación. Si hay varios bloqueos,
   se toma el más restrictivo.
4. **Semáforo (RF06).** `progreso = horasTranscurridas / horasRequeridas`, tomando el menor entre el progreso
   directo y el de adyacencia.

   | Progreso | Semáforo |
   |---|---|
   | < 0,5 | 🔴 ROJO (bloqueada) |
   | 0,5 – 0,99 | 🟡 AMARILLO (en recuperación) |
   | ≥ 1 | 🟢 VERDE (disponible) |

5. **Resumen por zona macro (RF09).**
   - `promedio = totalUsos30d / 4`
   - **Saturada** si `usos30d(zona) > promedio` → factor × 0,6
   - **Olvidada** si nunca se usó o lleva 15 días o más sin uso → factor × 1,5

## 4. Algoritmo `recomendar(evaluacion)` con Max-Heap (RF09)

1. Cada microzona se inserta en un **`MonticuloMaximo`** (montículo binario propio) en O(log n), ordenada por:
   1. nivel del semáforo (VERDE 2 > AMARILLO 1 > ROJO 0),
   2. puntaje: `horasDescanso × factorZona` si está VERDE, o `progreso × factorZona` si no,
   3. id, para desempatar de forma determinista.
2. `extraerMaximo()` en O(log n) devuelve la **zona sugerida**. Las 3 extracciones siguientes son las **alternativas**.
3. Si ninguna está en VERDE, se sugiere la de mayor recuperación y `hayZonasOptimas = false`.
4. Se devuelve el **motivo** en texto y el `tiempoCalculoMs`.

### Ejemplo

Una paciente con 12 aplicaciones en el abdomen y 1 en muslos en los últimos 30 días:

- Promedio = 13 / 4 = 3,25 → el **abdomen** (12) queda **saturado** (× 0,6).
- Brazos y glúteos no se usan hace 15 días o más → **olvidadas** (× 1,5).
- Resultado: la sugerencia cae en una microzona VERDE de **brazos o glúteos**. Esto corrige el *sesgo de comodidad* del caso de estudio.

## 5. Estructuras de datos

| Estructura | Clase | Uso | Complejidad |
|---|---|---|---|
| Matriz 2D | `CuadriculaZona` (`Microzona[][]`) | Cuadrícula de cada zona y lado | O(1) por celda |
| Tabla hash | `MapaEstados`, `MapaCorporal.indice` | Estado por id de microzona | O(1) |
| Grafo no dirigido | `GrafoAdyacencia` | Topología de la piel y bloqueo de vecinas | O(grado) |
| Max-Heap | `MonticuloMaximo<T>` | Extraer la microzona más recuperada | O(log n) |
| Pila LIFO | `PilaDeshacer` | Deshacer el último registro (persistida en el paciente, capacidad 20) | O(1) |
| Cola FIFO | `ColaAlertas<T>` | Despacho cronológico de recordatorios | O(1) |

## 6. Deshacer (RF05)

El estado del mapa **se deriva del historial**. Por eso, deshacer marca la aplicación de la cima de la pila
como `deshecha = true` y la microzona y sus vecinas vuelven solas al estado previo en la siguiente evaluación.
El registro no se borra: queda para auditoría, pero no cuenta en el mapa ni en el PDF.
