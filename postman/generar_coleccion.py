"""Genera la coleccion y los entornos de Postman de Insumap.

Uso: python postman/generar_coleccion.py
"""
import json
import os
import uuid

SALIDA = os.path.dirname(os.path.abspath(__file__))


def cuerpo(obj):
    return {"mode": "raw", "raw": json.dumps(obj, indent=2, ensure_ascii=False),
            "options": {"raw": {"language": "json"}}}


def peticion(nombre, metodo, ruta, datos=None, pruebas=None, descripcion="", query=None):
    url = {"raw": "{{baseUrl}}" + ruta, "host": ["{{baseUrl}}"], "path": ruta.strip("/").split("/")}
    if query:
        url["query"] = [{"key": k, "value": v} for k, v in query]
        url["raw"] += "?" + "&".join(f"{k}={v}" for k, v in query)
    item = {"name": nombre, "request": {"method": metodo, "url": url, "description": descripcion,
                                        "header": [{"key": "Content-Type", "value": "application/json"}]
                                        if datos is not None else []}}
    if datos is not None:
        item["request"]["body"] = cuerpo(datos)
    if pruebas:
        item["event"] = [{"listen": "test", "script": {"type": "text/javascript", "exec": pruebas}}]
    return item


PREVIO = ["// Documento aleatorio para poder ejecutar la coleccion varias veces sin duplicados",
          "if (pm.info.requestName === 'Crear paciente' || !pm.collectionVariables.get('documento')) {",
          "  const base = Date.now().toString().slice(-9);",
          "  pm.collectionVariables.set('documento', '1' + base);",
          "  pm.collectionVariables.set('documento2', '2' + base);",
          "}"]


def estado(codigo):
    return [f"pm.test('Codigo {codigo}', () => pm.response.to.have.status({codigo}));"]


def guardar(variable, expresion):
    return [f"if (pm.response.code < 300) {{ pm.collectionVariables.set('{variable}', pm.response.json(){expresion}); }}"]


PACIENTE = {
    "nombres": "Ana Maria", "apellidos": "Gomez Rojas", "documentoIdentidad": "{{documento}}",
    "fechaNacimiento": "1990-05-10", "tipoDiabetes": "TIPO_1", "email": "ana.gomez@example.com",
    "telefono": "3001234567", "configuracionMapa": {"filas": 3, "columnas": 4},
    "cuidadores": [{"nombre": "Luis Gomez", "parentesco": "Hermano", "telefono": "3009876543"}],
    "medicoTratante": {"nombre": "Dra. Laura Perez", "especialidad": "Endocrinologia", "email": "lperez@example.com"},
}
PACIENTE_2 = dict(PACIENTE, nombres="Carlos", apellidos="Ruiz", documentoIdentidad="{{documento2}}",
                  tipoDiabetes="TIPO_2", fechaNacimiento="1952-03-02",
                  cuidadores=[{"nombre": "Marta Ruiz", "parentesco": "Hija"}])
PACIENTE_ACTUALIZADO = dict(PACIENTE, telefono="3110000000")
P = "/api/pacientes/{{pacienteId}}"

coleccion = {
    "info": {
        "_postman_id": str(uuid.uuid4()),
        "name": "Insumap API",
        "description": (
            "Coleccion del backend Insumap (Spring Boot + MongoDB).\n\n"
            "Ejecute las carpetas en orden (o con el Collection Runner): los scripts de prueba guardan "
            "automaticamente pacienteId, aplicacionId, cronogramaId y recordatorioId.\n\n"
            "Seleccione el entorno 'Insumap - Local' o 'Insumap - Produccion'."),
        "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
    },
    "variable": [{"key": k, "value": ""} for k in
                 ["documento", "documento2", "pacienteId", "pacienteId2", "aplicacionId", "cronogramaId", "recordatorioId", "microzonaSugerida"]],
    "event": [{"listen": "prerequest", "script": {"type": "text/javascript", "exec": PREVIO}}],
    "item": [
        {"name": "0. Salud y catalogo", "item": [
            peticion("Salud del servicio", "GET", "/actuator/health", pruebas=estado(200)),
            peticion("Catalogo de zonas macro (RF01, RF08)", "GET", "/api/catalogo/zonas", pruebas=estado(200) + [
                "pm.test('4 zonas macro', () => pm.expect(pm.response.json()).to.have.length(4));"]),
        ]},
        {"name": "1. Pacientes", "item": [
            peticion("Crear paciente", "POST", "/api/pacientes", PACIENTE, estado(201) + guardar("pacienteId", ".id")),
            peticion("Crear segundo paciente (adulto mayor con cuidador)", "POST", "/api/pacientes", PACIENTE_2,
                     estado(201) + guardar("pacienteId2", ".id")),
            peticion("Crear paciente duplicado (409)", "POST", "/api/pacientes", PACIENTE, estado(409)),
            peticion("Crear paciente invalido (400)", "POST", "/api/pacientes", {"nombres": "", "tipoDiabetes": "TIPO_1"},
                     estado(400)),
            peticion("Listar pacientes", "GET", "/api/pacientes", pruebas=estado(200)),
            peticion("Consultar paciente", "GET", P, pruebas=estado(200)),
            peticion("Actualizar paciente", "PUT", P, PACIENTE_ACTUALIZADO, estado(200)),
            peticion("Configurar cuadricula 3x4 (RF02)", "PUT", P + "/configuracion-mapa", {"filas": 3, "columnas": 4},
                     estado(200) + ["pm.test('96 microzonas', () => "
                                    "pm.expect(pm.response.json().configuracionMapa.totalMicrozonas).to.eql(96));"]),
            peticion("Paciente inexistente (404)", "GET", "/api/pacientes/000000000000000000000000",
                     pruebas=estado(404)),
        ]},
        {"name": "2. Mapa corporal y recomendacion", "item": [
            peticion("Mapa corporal 2D con semaforo (RF01, RF06)", "GET", P + "/mapa", pruebas=estado(200)),
            peticion("Zona sugerida (RF08, RF09)", "GET", P + "/recomendacion",
                     pruebas=estado(200) + guardar("microzonaSugerida", ".microzona.id") + [
                         "pm.test('Calculo menor a 200 ms', () => "
                         "pm.expect(pm.response.json().tiempoCalculoMs).to.be.below(200));"]),
            peticion("Detalle de microzona y vecinas (grafo)", "GET", P + "/mapa/microzonas/abd_izq_06",
                     pruebas=estado(200)),
            peticion("Estadisticas de rotacion", "GET", P + "/estadisticas", pruebas=estado(200)),
        ]},
        {"name": "3. Aplicaciones", "item": [
            peticion("Registrar en la zona sugerida (RF03, RF07)", "POST", P + "/aplicaciones",
                     {"microzonaId": "{{microzonaSugerida}}", "tipoInsulina": "RAPIDA", "unidades": 6,
                      "registradoPor": "PACIENTE"},
                     estado(201) + guardar("aplicacionId", ".aplicacion.id") + [
                         "pm.test('Bloquea la microzona y sus adyacentes', () => "
                         "pm.expect(pm.response.json().microzonasBloqueadas.length).to.be.above(1));"]),
            peticion("Registrar en abd_izq_04", "POST", P + "/aplicaciones",
                     {"microzonaId": "abd_izq_04", "tipoInsulina": "RAPIDA", "unidades": 5.5},
                     estado(201) + guardar("aplicacionId", ".aplicacion.id")),
            peticion("Validar microzona bloqueada antes de registrar (RF04)", "GET",
                     P + "/aplicaciones/validar/abd_izq_04", pruebas=estado(200) + [
                         "pm.test('Advierte sin bloquear', () => { const r = pm.response.json(); "
                         "pm.expect(r.permitido).to.eql(true); pm.expect(r.requiereAdvertencia).to.eql(true); });"]),
            peticion("Registrar en microzona ROJA: se permite con advertencia (RF04)", "POST", P + "/aplicaciones",
                     {"microzonaId": "abd_izq_04", "tipoInsulina": "RAPIDA", "unidades": 5,
                      "registradoPor": "CUIDADOR", "notas": "Registrado por el cuidador"},
                     estado(201) + guardar("aplicacionId", ".aplicacion.id") + [
                         "pm.test('Trae advertencia', () => pm.expect(pm.response.json().advertencia).to.be.a('string'));"]),
            peticion("Deshacer ultimo registro (RF05)", "POST", P + "/aplicaciones/deshacer", pruebas=estado(200)),
            peticion("Registrar aplicacion pasada (fecha manual)", "POST", P + "/aplicaciones",
                     {"microzonaId": "mus_der_02", "tipoInsulina": "PROLONGADA", "unidades": 18,
                      "fechaHora": "2026-11-20T13:00:00Z"}, estado(201)),
            peticion("Registrar con fecha futura (400)", "POST", P + "/aplicaciones",
                     {"microzonaId": "mus_der_03", "tipoInsulina": "RAPIDA", "unidades": 4,
                      "fechaHora": "2099-01-01T00:00:00Z"}, estado(400)),
            peticion("Registrar en microzona inexistente (404)", "POST", P + "/aplicaciones",
                     {"microzonaId": "abd_izq_99", "tipoInsulina": "RAPIDA", "unidades": 4}, estado(404)),
            peticion("Historial completo (RF11)", "GET", P + "/aplicaciones", pruebas=estado(200)),
            peticion("Historial filtrado por zona", "GET", P + "/aplicaciones", pruebas=estado(200),
                     query=[("zona", "ABDOMEN")]),
            peticion("Consultar aplicacion", "GET", P + "/aplicaciones/{{aplicacionId}}", pruebas=estado(200)),
            peticion("Exportar historial PDF (RF11)", "GET", P + "/aplicaciones/exportar/pdf", pruebas=estado(200) + [
                "pm.test('Es un PDF', () => pm.expect(pm.response.headers.get('Content-Type')).to.include('application/pdf'));"],
                     descripcion="Use 'Send and Download' para guardar el archivo PDF."),
        ]},
        {"name": "4. Cronogramas", "item": [
            peticion("Crear cronograma (RF10)", "POST", P + "/cronogramas",
                     {"nombre": "Insulina basal", "tipoInsulina": "PROLONGADA", "unidades": 20,
                      "horas": ["07:00", "21:00"],
                      "diasSemana": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"]},
                     estado(201) + guardar("cronogramaId", ".id")),
            peticion("Listar cronogramas", "GET", P + "/cronogramas", pruebas=estado(200)),
            peticion("Actualizar cronograma", "PUT", P + "/cronogramas/{{cronogramaId}}",
                     {"nombre": "Insulina basal", "tipoInsulina": "PROLONGADA", "unidades": 22,
                      "horas": ["07:00", "21:00"]}, estado(200)),
            peticion("Hora invalida (400)", "POST", P + "/cronogramas",
                     {"nombre": "X", "tipoInsulina": "RAPIDA", "unidades": 4, "horas": ["25:00"]}, estado(400)),
        ]},
        {"name": "5. Recordatorios", "item": [
            peticion("Procesar ciclo (generar y despachar con cola FIFO)", "POST", "/api/recordatorios/procesar",
                     pruebas=estado(200)),
            peticion("Listar recordatorios del paciente", "GET", P + "/recordatorios", pruebas=estado(200) + [
                "if (pm.response.json().length) { pm.collectionVariables.set('recordatorioId', pm.response.json()[0].id); }"]),
            peticion("Aplazar recordatorio 10 minutos", "POST", "/api/recordatorios/{{recordatorioId}}/aplazar",
                     {"minutos": 10}, estado(200)),
            peticion("Confirmar dosis y registrar inyeccion", "POST", "/api/recordatorios/{{recordatorioId}}/confirmar",
                     {"microzonaId": "glu_izq_05", "registradoPor": "PACIENTE"}, estado(200)),
            peticion("Confirmar de nuevo (409)", "POST", "/api/recordatorios/{{recordatorioId}}/confirmar", {},
                     estado(409)),
            peticion("Desactivar cronograma", "PATCH", P + "/cronogramas/{{cronogramaId}}/estado",
                     pruebas=estado(200), query=[("activo", "false")]),
        ]},
        {"name": "6. Limpieza", "item": [
            peticion("Eliminar cronograma", "DELETE", P + "/cronogramas/{{cronogramaId}}", pruebas=estado(204)),
            peticion("Eliminar segundo paciente", "DELETE", "/api/pacientes/{{pacienteId2}}", pruebas=estado(204)),
        ]},
    ],
}

with open(os.path.join(SALIDA, "Insumap.postman_collection.json"), "w", encoding="utf-8") as f:
    json.dump(coleccion, f, indent=2, ensure_ascii=False)

for nombre, url in [("Local", "http://localhost:8080"), ("Produccion", "https://insumap-diseno.onrender.com")]:
    entorno = {"id": str(uuid.uuid4()), "name": f"Insumap - {nombre}",
               "values": [{"key": "baseUrl", "value": url, "type": "default", "enabled": True}],
               "_postman_variable_scope": "environment"}
    with open(os.path.join(SALIDA, f"Insumap-{nombre}.postman_environment.json"), "w", encoding="utf-8") as f:
        json.dump(entorno, f, indent=2, ensure_ascii=False)

print("Peticiones:", sum(len(c["item"]) for c in coleccion["item"]))
