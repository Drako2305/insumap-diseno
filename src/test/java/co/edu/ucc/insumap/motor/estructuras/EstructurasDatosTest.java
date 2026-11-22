package co.edu.ucc.insumap.motor.estructuras;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.ucc.insumap.modelo.enums.Lado;
import co.edu.ucc.insumap.modelo.enums.ZonaMacro;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EstructurasDatosTest {

    @Test
    @DisplayName("La matriz 2D genera ids secuenciales por filas (abd_izq_01 .. abd_izq_12)")
    void cuadriculaGeneraIds() {
        CuadriculaZona cuadricula = new CuadriculaZona(ZonaMacro.ABDOMEN, Lado.IZQUIERDO, 3, 4);

        assertThat(cuadricula.obtener(0, 0).id()).isEqualTo("abd_izq_01");
        assertThat(cuadricula.obtener(0, 3).id()).isEqualTo("abd_izq_04");
        assertThat(cuadricula.obtener(2, 3).id()).isEqualTo("abd_izq_12");
        assertThat(cuadricula.obtener(3, 0)).isNull();
        assertThat(cuadricula.todas()).hasSize(12);
    }

    @Test
    void cuadriculaRechazaTamanoInvalido() {
        assertThatThrownBy(() -> new CuadriculaZona(ZonaMacro.BRAZOS, Lado.DERECHO, 0, 3))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("El grafo es no dirigido")
    void grafoNoDirigido() {
        GrafoAdyacencia grafo = new GrafoAdyacencia();
        grafo.agregarArista("a", "b");
        grafo.agregarArista("b", "c");
        grafo.agregarArista("a", "a");

        assertThat(grafo.sonAdyacentes("a", "b")).isTrue();
        assertThat(grafo.sonAdyacentes("b", "a")).isTrue();
        assertThat(grafo.sonAdyacentes("a", "c")).isFalse();
        assertThat(grafo.vecinos("b")).containsExactlyInAnyOrder("a", "c");
        assertThat(grafo.cantidadNodos()).isEqualTo(3);
        assertThat(grafo.cantidadAristas()).isEqualTo(2);
    }

    @Test
    @DisplayName("El Max-Heap extrae siempre el mayor elemento")
    void monticuloExtraeEnOrdenDescendente() {
        MonticuloMaximo<Integer> monticulo = new MonticuloMaximo<>(Comparator.<Integer>naturalOrder());
        List<Integer> valores = new Random(7).ints(200, 0, 1000).boxed().toList();
        valores.forEach(monticulo::insertar);

        List<Integer> extraidos = new ArrayList<>();
        while (!monticulo.estaVacio()) {
            extraidos.add(monticulo.extraerMaximo());
        }

        assertThat(extraidos).hasSize(200).isSortedAccordingTo(Comparator.reverseOrder());
    }

    @Test
    void monticuloVacioLanzaExcepcion() {
        MonticuloMaximo<Integer> monticulo = new MonticuloMaximo<>(Comparator.<Integer>naturalOrder());
        assertThatThrownBy(monticulo::extraerMaximo).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("La pila de deshacer es LIFO y se puede persistir y reconstruir")
    void pilaLifo() {
        PilaDeshacer pila = PilaDeshacer.desdeLista(List.of("a1", "a2", "a3"));

        assertThat(pila.cima()).contains("a3");
        assertThat(pila.aLista()).containsExactly("a1", "a2", "a3");
        assertThat(pila.desapilar()).contains("a3");
        assertThat(pila.desapilar()).contains("a2");
        assertThat(pila.tamano()).isEqualTo(1);
    }

    @Test
    void pilaDescartaLoMasAntiguoAlLlenarse() {
        PilaDeshacer pila = new PilaDeshacer(2);
        pila.apilar("a1");
        pila.apilar("a2");
        pila.apilar("a3");

        assertThat(pila.aLista()).containsExactly("a2", "a3");
    }

    @Test
    @DisplayName("La cola de alertas es FIFO")
    void colaFifo() {
        ColaAlertas<String> cola = new ColaAlertas<>();
        cola.encolar("07:00");
        cola.encolar("13:00");
        cola.encolar("19:00");

        assertThat(cola.desencolar()).contains("07:00");
        assertThat(cola.frente()).contains("13:00");
        assertThat(cola.tamano()).isEqualTo(2);
    }
}
