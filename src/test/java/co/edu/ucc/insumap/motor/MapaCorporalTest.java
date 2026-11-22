package co.edu.ucc.insumap.motor;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.ucc.insumap.modelo.ConfiguracionMapa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MapaCorporalTest {

    private final MapaCorporal mapa = new MapaCorporal(new ConfiguracionMapa(3, 4));

    @Test
    @DisplayName("4 zonas x 2 lados x 3 x 4 = 96 microzonas")
    void totalMicrozonas() {
        assertThat(mapa.totalMicrozonas()).isEqualTo(96);
        assertThat(mapa.getGrafo().cantidadNodos()).isEqualTo(96);
        assertThat(mapa.cuadriculas()).hasSize(8);
    }

    @Test
    @DisplayName("Una esquina tiene 3 vecinas y una celda interior 8")
    void vecindadDeOchoDirecciones() {
        assertThat(mapa.vecinos("mus_izq_01")).containsExactlyInAnyOrder("mus_izq_02", "mus_izq_05", "mus_izq_06");
        assertThat(mapa.vecinos("mus_izq_06")).hasSize(8);
    }

    @Test
    @DisplayName("En el abdomen los dos lados se conectan por la linea media")
    void lineaMediaAbdomen() {
        assertThat(mapa.vecinos("abd_izq_04")).contains("abd_der_01", "abd_der_05");
        assertThat(mapa.getGrafo().sonAdyacentes("abd_der_01", "abd_izq_04")).isTrue();
    }

    @Test
    @DisplayName("Zonas distintas o lados distintos fuera del abdomen no son adyacentes")
    void sinAdyacenciaEntreZonas() {
        assertThat(mapa.vecinos("bra_izq_04")).noneMatch(id -> id.startsWith("bra_der"));
        assertThat(mapa.vecinos("glu_der_01")).allMatch(id -> id.startsWith("glu_der"));
    }

    @Test
    void buscarIgnoraMayusculasYRechazaInexistentes() {
        assertThat(mapa.buscar("ABD_IZQ_04")).isPresent();
        assertThat(mapa.buscar("abd_izq_13")).isEmpty();
        assertThat(mapa.buscar(null)).isEmpty();
    }
}
