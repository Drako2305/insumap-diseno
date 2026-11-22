# Datos de ejemplo para MongoDB

Datos listos para cargar en la base `insumap`, de modo que la API arranque con información para probar.
Cada archivo es una colección con el mismo formato que guarda el backend.

| Archivo | Colección | Contenido |
|---|---|---|
| `pacientes.json` | `pacientes` | 2 pacientes (Tipo 1 y Tipo 2) con cuadrícula 3x4, cuidador y médico tratante |
| `aplicaciones.json` | `aplicaciones` | 5 inyecciones en abdomen, brazos, muslos y glúteos |
| `cronogramas.json` | `cronogramas` | Insulina basal (07:00 y 21:00) y bolo de almuerzo |
| `recordatorios.json` | `recordatorios` | Un recordatorio confirmado y uno aplazado |

El paciente principal es `6b1000000000000000000001`. Úsalo en Postman como `pacienteId`.

## Cargar con MongoDB Compass

1. Conéctate a `mongodb://localhost:27017` (local) o a la cadena de MongoDB Atlas.
2. Crea la base de datos `insumap` si no existe.
3. Por cada archivo: crea la colección con el nombre de la tabla de arriba → **ADD DATA** → **Import JSON or CSV file** → elige el archivo → **Import**.

## Cargar con `mongoimport`

```bash
mongoimport --db insumap --collection pacientes     --jsonArray --file mongo/pacientes.json
mongoimport --db insumap --collection aplicaciones  --jsonArray --file mongo/aplicaciones.json
mongoimport --db insumap --collection cronogramas   --jsonArray --file mongo/cronogramas.json
mongoimport --db insumap --collection recordatorios --jsonArray --file mongo/recordatorios.json
```

Para MongoDB Atlas, agrega `--uri "mongodb+srv://USUARIO:CLAVE@cluster0.xxxxx.mongodb.net/insumap"`.

## Consultas útiles (mongosh o la pestaña de filtros de Compass)

```js
// Historial activo de un paciente, del más reciente al más antiguo
db.aplicaciones.find({ pacienteId: "6b1000000000000000000001", deshecha: false }).sort({ fechaHora: -1 })

// Uso por zona macro (base de la regla de zonas saturadas, RF09)
db.aplicaciones.aggregate([
  { $match: { pacienteId: "6b1000000000000000000001", deshecha: false } },
  { $group: { _id: "$zonaMacro", usos: { $sum: 1 } } }
])

// Recordatorios abiertos
db.recordatorios.find({ estado: { $in: ["PENDIENTE", "ENVIADO", "APLAZADO"] } })
```

## Índices que crea la aplicación

| Colección | Índice | Tipo |
|---|---|---|
| `pacientes` | `documentoIdentidad` | único |
| `aplicaciones` | `{ pacienteId: 1, fechaHora: -1 }` | compuesto |
| `cronogramas` | `pacienteId` | simple |
| `recordatorios` | `{ cronogramaId: 1, fechaOriginal: 1 }` | compuesto único |
