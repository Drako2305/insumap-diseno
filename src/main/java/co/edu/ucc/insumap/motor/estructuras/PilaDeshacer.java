package co.edu.ucc.insumap.motor.estructuras;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/**
 * Pila LIFO con los identificadores de las ultimas aplicaciones registradas (RF05).
 * La cima es la ultima aplicacion; "Deshacer" la desapila. Tiene capacidad acotada:
 * al superarla se descarta la entrada mas antigua (el fondo de la pila).
 */
public class PilaDeshacer {

    public static final int CAPACIDAD_POR_DEFECTO = 20;

    private final Deque<String> pila = new ArrayDeque<>();
    private final int capacidad;

    public PilaDeshacer(int capacidad) {
        this.capacidad = capacidad;
    }

    /** Reconstruye la pila desde su forma persistida (indice 0 = fondo, ultimo = cima). */
    public static PilaDeshacer desdeLista(List<String> persistida) {
        PilaDeshacer pila = new PilaDeshacer(CAPACIDAD_POR_DEFECTO);
        if (persistida != null) {
            persistida.forEach(pila::apilar);
        }
        return pila;
    }

    public void apilar(String aplicacionId) {
        pila.push(aplicacionId);
        if (pila.size() > capacidad) {
            pila.removeLast();
        }
    }

    public Optional<String> desapilar() {
        return Optional.ofNullable(pila.poll());
    }

    public Optional<String> cima() {
        return Optional.ofNullable(pila.peek());
    }

    public boolean estaVacia() {
        return pila.isEmpty();
    }

    public int tamano() {
        return pila.size();
    }

    /** Forma persistible: indice 0 = fondo, ultimo = cima. */
    public List<String> aLista() {
        List<String> lista = new ArrayList<>(pila);
        java.util.Collections.reverse(lista);
        return lista;
    }
}
