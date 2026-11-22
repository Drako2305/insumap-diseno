# Despliegue de Insumap

| Componente | Servicio | Plan |
|---|---|---|
| Backend Spring Boot (contenedor Docker) | [Render](https://render.com) – Web Service | Free |
| Base de datos | [MongoDB Atlas](https://www.mongodb.com/atlas) – Cluster M0 | Free |
| Código fuente y CI de despliegue | GitHub (`main` → Render despliega automáticamente) | Free |

```
Cliente (Postman / navegador) --HTTPS--> Render (contenedor Docker: JRE 21 + insumap.jar)
                                              |
                                              +--TLS (mongodb+srv)--> MongoDB Atlas (cluster M0)
```

## 1. Crear la base de datos en MongoDB Atlas

1. Crear una cuenta en MongoDB Atlas y un cluster **M0 (Free)**.
2. *Database Access* → **Add New Database User**: usuario `insumap` con una contraseña segura y rol *Read and write to any database*.
3. *Network Access* → **Add IP Address** → `0.0.0.0/0`. Render no tiene IP fija en el plan gratuito.
4. *Connect* → *Drivers* → copiar la cadena y agregar el nombre de la base de datos:

   ```
   mongodb+srv://insumap:<contraseña>@<cluster>.mongodb.net/insumap?retryWrites=true&w=majority
   ```

## 2. Crear el servicio en Render

1. Entrar a Render con la cuenta de GitHub.
2. **New → Blueprint** → seleccionar el repositorio `insumap-diseno`. Render lee `render.yaml`.
3. Cuando pida `MONGODB_URI`, pegar la cadena de Atlas.
4. Esperar el build del `Dockerfile`, que tarda entre 5 y 8 minutos la primera vez.
5. Verificar:
   - `https://insumap-diseno.onrender.com/actuator/health` → `{"status":"UP"}`
   - `https://insumap-diseno.onrender.com/swagger-ui.html`

> En el plan gratuito, el servicio se duerme tras 15 minutos sin tráfico. La primera petición
> después de eso tarda unos 50 segundos en responder.

## 3. Probar con Postman

Importar `postman/Insumap.postman_collection.json` y `postman/Insumap-Produccion.postman_environment.json`.
Si Render asignó otra URL, cambiar `baseUrl` en el entorno. Luego ejecutar la colección con el *Collection Runner*.

## Ejecución local con Docker (opcional)

```bash
docker build -t insumap .
docker run -p 8080:8080 -e MONGODB_URI="mongodb://host.docker.internal:27017/insumap" insumap
```

## Variables de entorno

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `MONGODB_URI` | Cadena de conexión de MongoDB | `mongodb://localhost:27017/insumap` |
| `PORT` | Puerto HTTP (Render lo inyecta) | `8080` |
| `JAVA_OPTS` | Opciones de la JVM | `-XX:MaxRAMPercentage=75 -XX:+UseSerialGC` |
