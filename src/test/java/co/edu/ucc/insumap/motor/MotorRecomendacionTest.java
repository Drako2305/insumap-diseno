package co.edu.ucc.insumap.motor;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.ucc.insumap.config.ParametrosMotor;
import co.edu.ucc.insumap.modelo.Aplicacion;
import co.edu.ucc.insumap.modelo.ConfiguracionMapa;
import co.edu.ucc.insumap.modelo.enums.Semaforo;
import co.edu.ucc.insumap.modelo.enums.ZonaMacro;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MotorRecomendacionTest {

    private static final Instant AHORA = Instant.parse("2026-11-22T12:00:00Z");

    private final ParametrosMotor parametros = ParametrosMotor.porDefecto();
    private final CalculadoraRecuperacion calculadora = new CalculadoraRecuperacion(parametros);
    private final MotorRecomendacion motor = new MotorRecomendacion(parametros, calculadora);
    private final MapaCorporal mapa = new MapaCorporal(new ConfiguracionMapa(3, 4));

    private static Aplicacion aplicacion(String microzonaId, Duration haceCuanto) {
        Aplicacion a = new Aplicacion();
        a.setMicrozonaId(microzonaId);
        a.setFechaHora(AHORA.minus(haceCuanto));
        return a;
    }

    private Semaforo semaforo(ResultadoEvaluacion r, String id) {
        return r.estados().obtener(id).orElseThrow().getSemaforo();
    }

    @Test
    @DisplayName("Sin historial todas las microzonas estan en VERDE y todas las zonas son olvidadas")
    void sinHistorial() {
        ResultadoEvaluacion r = motor.evaluar(mapa, List.of(), AHORA);

        assertThat(r.estados().contar(Semaforo.VERDE)).isEqualTo(96);
        assertThat(r.resumenZonas().values()).allMatch(ResumenZonaMacro::olvidada);
        assertThat(motor.recomendar(r).hayZonasOptimas()).isTrue();
    }

    @Test
    @DisplayName("RF07: la microzona usada y sus adyacentes quedan en ROJO")
    void bloqueoPorAdyacencia() {
        ResultadoEvaluacion r = motor.evaluar(mapa, List.of(aplicacion("abd_izq_06", Duration.ZERO)), AHORA);

        assertThat(semaforo(r, "abd_izq_06")).isEqualTo(Semaforo.ROJO);
        for (String vecino : mapa.vecinos("abd_izq_06")) {
            EstadoMicrozona estado = r.estados().obtener(vecino).orElseThrow();
            assertThat(estado.getSemaforo()).isEqualTo(Semaforo.ROJO);
            assertThat(estado.isBloqueadaPorAdyacencia()).isTrue();
            assertThat(estado.getOrigenBloqueo()).isEqualTo("abd_izq_06");
        }
        assertThat(r.estados().contar(Semaforo.ROJO)).isEqualTo(9);
        assertThat(semaforo(r, "abd_izq_12")).isEqualTo(Semaforo.VERDE);
    }

    @Test
    @DisplayName("RF06: abdomen (72 h) pasa de ROJO a AMARILLO y luego a VERDE")
    void semaforoEnElTiempo() {
        assertThat(semaforo(motor.evaluar(mapa, List.of(aplicacion("abd_izq_01", Duration.ofHours(20))), AHORA),
                "abd_izq_01")).isEqualTo(Semaforo.ROJO);
        ResultadoEvaluacion amarillo = motor.evaluar(mapa, List.of(aplicacion("abd_izq_01", Duration.ofHours(50))), AHORA);
        assertThat(semaforo(amarillo, "abd_izq_01")).isEqualTo(Semaforo.AMARILLO);
        assertThat(semaforo(amarillo, "abd_izq_02")).isEqualTo(Semaforo.VERDE); // adyacente: 36 h
        assertThat(semaforo(motor.evaluar(mapa, List.of(aplicacion("abd_izq_01", Duration.ofHours(73))), AHORA),
                "abd_izq_01")).isEqualTo(Semaforo.VERDE);
    }

    @Test
    @DisplayName("RF08: cada uso extra de la microzona en la ventana alarga la recuperacion un 25 %")
    void frecuenciaAlargaRecuperacion() {
        List<Aplicacion> historial = List.of(aplicacion("glu_der_05", Duration.ofDays(10)),
                aplicacion("glu_der_05", Duration.ofDays(15)), aplicacion("glu_der_05", Duration.ofDays(20)));
        EstadoMicrozona estado = motor.evaluar(mapa, historial, AHORA).estados().obtener("glu_der_05").orElseThrow();

        assertThat(estado.getUsosEnVentana()).isEqualTo(3);
        assertThat(estado.getHorasRecuperacionRequeridas()).isEqualTo(120 * 1.5);
        assertThat(calculadora.horasRecuperacion(ZonaMacro.GLUTEOS, 50)).isEqualTo(240); // tope x2
    }

    @Test
    @DisplayName("RF09: penaliza la zona saturada y prioriza las zonas olvidadas")
    void saturadaYOlvidada() {
        List<Aplicacion> historial = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            historial.add(aplicacion(String.format("abd_izq_%02d", i), Duration.ofDays(i * 2L)));
        }
        historial.add(aplicacion("mus_izq_01", Duration.ofDays(3)));
        historial.add(aplicacion("bra_izq_01", Duration.ofDays(20)));

        ResultadoEvaluacion r = motor.evaluar(mapa, historial, AHORA);
        Recomendacion recomendacion = motor.recomendar(r);

        assertThat(r.resumenZonas().get(ZonaMacro.ABDOMEN).saturada()).isTrue();
        assertThat(r.resumenZonas().get(ZonaMacro.MUSLOS).olvidada()).isFalse();
        assertThat(r.resumenZonas().get(ZonaMacro.BRAZOS).olvidada()).isTrue();
        assertThat(r.resumenZonas().get(ZonaMacro.GLUTEOS).olvidada()).isTrue();
        assertThat(recomendacion.zonasSaturadas()).containsExactly(ZonaMacro.ABDOMEN);
        assertThat(recomendacion.principal().estado().getMicrozona().zonaMacro())
                .isIn(ZonaMacro.BRAZOS, ZonaMacro.GLUTEOS);
        assertThat(recomendacion.principal().estado().getSemaforo()).isEqualTo(Semaforo.VERDE);
        assertThat(recomendacion.alternativas()).hasSize(parametros.alternativas());
    }

    @Test
    @DisplayName("Las aplicaciones deshechas o fuera de la cuadricula no afectan el mapa")
    void ignoraDeshechasYFueraDeCuadricula() {
        Aplicacion deshecha = aplicacion("abd_izq_01", Duration.ZERO);
        deshecha.setDeshecha(true);
        ResultadoEvaluacion r = motor.evaluar(mapa,
                List.of(deshecha, aplicacion("abd_izq_99", Duration.ZERO)), AHORA);

        assertThat(r.estados().contar(Semaforo.VERDE)).isEqualTo(96);
    }

    @Test
    @DisplayName("Requisito no funcional: evaluar y recomendar en menos de 200 ms (cuadricula 8x8, 1000 registros)")
    void rendimiento() {
        MapaCorporal grande = new MapaCorporal(new ConfiguracionMapa(8, 8));
        List<String> ids = grande.todas().stream().map(m -> m.id()).toList();
        Random azar = new Random(11);
        List<Aplicacion> historial = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            historial.add(aplicacion(ids.get(azar.nextInt(ids.size())), Duration.ofHours(azar.nextInt(24 * 60))));
        }
        motor.recomendar(motor.evaluar(grande, historial, AHORA)); // calentamiento de la JVM

        long inicio = System.nanoTime();
        Recomendacion recomendacion = motor.recomendar(motor.evaluar(grande, historial, AHORA));
        double ms = (System.nanoTime() - inicio) / 1_000_000.0;

        assertThat(recomendacion.principal()).isNotNull();
        assertThat(ms).isLessThan(200);
    }
}
