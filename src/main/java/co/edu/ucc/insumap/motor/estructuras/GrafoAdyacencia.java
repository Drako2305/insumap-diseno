package co.edu.ucc.insumap.motor.estructuras;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Grafo no dirigido que modela la topologia de la piel: cada microzona es un nodo y las
 * microzonas vecinas se conectan con aristas (RF07). Se implementa con listas de adyacencia.
 */
public class GrafoAdyacencia {

    private final Map<String, Set<String>> adyacencias = new HashMap<>();

    public void agregarNodo(String nodo) {
        adyacencias.computeIfAbsent(nodo, k -> new LinkedHashSet<>());
    }

    /** Agrega una arista no dirigida: a-b implica b-a. */
    public void agregarArista(String a, String b) {
        if (a.equals(b)) {
            return;
        }
        agregarNodo(a);
        agregarNodo(b);
        adyacencias.get(a).add(b);
        adyacencias.get(b).add(a);
    }

    public Set<String> vecinos(String nodo) {
        return Collections.unmodifiableSet(adyacencias.getOrDefault(nodo, Set.of()));
    }

    public boolean sonAdyacentes(String a, String b) {
        return adyacencias.getOrDefault(a, Set.of()).contains(b);
    }

    public boolean contieneNodo(String nodo) {
        return adyacencias.containsKey(nodo);
    }

    public int cantidadNodos() {
        return adyacencias.size();
    }

    public int cantidadAristas() {
        int suma = 0;
        for (Set<String> vecinos : adyacencias.values()) {
            suma += vecinos.size();
        }
        return suma / 2;
    }
}
