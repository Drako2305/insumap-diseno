package co.edu.ucc.insumap.motor.estructuras;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Cola de prioridad implementada como monticulo binario maximo (Max-Heap).
 * Insertar y extraer el maximo cuestan O(log n); consultar el maximo cuesta O(1).
 * El motor la usa para extraer la microzona con mayor puntaje de descanso.
 *
 * @param <T> tipo de elemento almacenado
 */
public class MonticuloMaximo<T> {

    private final List<T> elementos = new ArrayList<>();
    private final Comparator<T> comparador;

    public MonticuloMaximo(Comparator<T> comparador) {
        this.comparador = comparador;
    }

    public void insertar(T elemento) {
        elementos.add(elemento);
        subir(elementos.size() - 1);
    }

    public T verMaximo() {
        if (estaVacio()) {
            throw new NoSuchElementException("El monticulo esta vacio");
        }
        return elementos.get(0);
    }

    public T extraerMaximo() {
        T maximo = verMaximo();
        T ultimo = elementos.remove(elementos.size() - 1);
        if (!elementos.isEmpty()) {
            elementos.set(0, ultimo);
            bajar(0);
        }
        return maximo;
    }

    public int tamano() {
        return elementos.size();
    }

    public boolean estaVacio() {
        return elementos.isEmpty();
    }

    private void subir(int indice) {
        while (indice > 0) {
            int padre = (indice - 1) / 2;
            if (comparador.compare(elementos.get(indice), elementos.get(padre)) <= 0) {
                return;
            }
            intercambiar(indice, padre);
            indice = padre;
        }
    }

    private void bajar(int indice) {
        int n = elementos.size();
        while (true) {
            int izquierdo = 2 * indice + 1;
            int derecho = izquierdo + 1;
            int mayor = indice;
            if (izquierdo < n && comparador.compare(elementos.get(izquierdo), elementos.get(mayor)) > 0) {
                mayor = izquierdo;
            }
            if (derecho < n && comparador.compare(elementos.get(derecho), elementos.get(mayor)) > 0) {
                mayor = derecho;
            }
            if (mayor == indice) {
                return;
            }
            intercambiar(indice, mayor);
            indice = mayor;
        }
    }

    private void intercambiar(int i, int j) {
        T temporal = elementos.get(i);
        elementos.set(i, elementos.get(j));
        elementos.set(j, temporal);
    }
}
