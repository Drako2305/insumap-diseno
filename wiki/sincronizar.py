"""Copia los diagramas y la guia de despliegue a las paginas de la wiki y, opcionalmente, publica la wiki.

Uso:
    python wiki/sincronizar.py                 # solo copia los archivos a wiki/
    python wiki/sincronizar.py <ruta-wiki-git>  # ademas copia todo a un clon de <repo>.wiki.git
"""
import os
import shutil
import sys

RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
WIKI = os.path.join(RAIZ, "wiki")

COPIAS = {
    "diagramas/README.md": "Notacion-UML.md",
    "diagramas/01-vista-contexto.md": "Vista-de-contexto.md",
    "diagramas/02-vista-funcional.md": "Vista-funcional.md",
    "diagramas/03-vista-logica.md": "Vista-logica.md",
    "diagramas/04-vista-desarrollo.md": "Vista-de-desarrollo.md",
    "diagramas/05-vista-despliegue.md": "Vista-de-despliegue.md",
    "despliegue/README.md": "Despliegue.md",
}

ENLACES = {
    "(01-vista-contexto.md)": "(Vista-de-contexto)",
    "(02-vista-funcional.md)": "(Vista-funcional)",
    "(03-vista-logica.md)": "(Vista-logica)",
    "(04-vista-desarrollo.md)": "(Vista-de-desarrollo)",
    "(05-vista-despliegue.md)": "(Vista-de-despliegue)",
    "(imagenes/)": "(https://github.com/Drako2305/insumap-diseno/tree/main/diagramas/imagenes)",
}

AVISO = ("> Página generada desde `{origen}` del repositorio "
         "(https://github.com/Drako2305/insumap-diseno/blob/main/{origen}).\n\n")

for origen, destino in COPIAS.items():
    with open(os.path.join(RAIZ, origen), encoding="utf-8") as f:
        texto = f.read()
    for viejo, nuevo in ENLACES.items():
        texto = texto.replace(viejo, nuevo)
    with open(os.path.join(WIKI, destino), "w", encoding="utf-8", newline="\n") as f:
        f.write(AVISO.format(origen=origen) + texto)
    print("copiado", origen, "->", destino)

if len(sys.argv) > 1:
    destino_git = sys.argv[1]
    for nombre in os.listdir(WIKI):
        if nombre.endswith(".md"):
            shutil.copy(os.path.join(WIKI, nombre), os.path.join(destino_git, nombre))
    print("wiki copiada a", destino_git)
