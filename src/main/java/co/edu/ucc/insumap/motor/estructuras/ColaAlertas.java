package co.edu.ucc.insumap.motor.estructuras;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;

/**
 * Cola FIFO que administra el despacho cronologico de las alertas de medicacion (RF10).
 * El primero en encolarse (la dosis mas antigua) es el primero en despacharse.
 *
 * @param <T> tipo de alerta
 */
public class ColaAlertas<T> {

    private final Queue<T> cola = new ArrayDeque<>();

    public synchronized void encolar(T alerta) {
        cola.offer(alerta);
    }

    public synchronized Optional<T> desencolar() {
        return Optional.ofNullable(cola.poll());
    }

    public synchronized Optional<T> frente() {
        return Optional.ofNullable(cola.peek());
    }

    public synchronized int tamano() {
        return cola.size();
    }

    public synchronized boolean estaVacia() {
        return cola.isEmpty();
    }
}
