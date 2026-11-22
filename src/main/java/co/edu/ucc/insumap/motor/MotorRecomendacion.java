package co.edu.ucc.insumap.motor;

import co.edu.ucc.insumap.config.ParametrosMotor;
import co.edu.ucc.insumap.modelo.Aplicacion;
import co.edu.ucc.insumap.modelo.enums.Semaforo;
import co.edu.ucc.insumap.modelo.enums.ZonaMacro;
import co.edu.ucc.insumap.motor.estructuras.MapaEstados;
import co.edu.ucc.insumap.motor.estructuras.Microzona;
import co.edu.ucc.insumap.motor.estructuras.MonticuloMaximo;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Motor de recomendacion heuristica (RF06 - RF09). No depende de la base de datos: recibe el mapa
 * corporal y el historial, y calcula el estado de cada microzona y la zona optima.
 */
@Component
public class MotorRecomendacion {

    private static final double HORAS_POR_DIA = 24.0;

    private final ParametrosMotor parametros;
    private final CalculadoraRecuperacion calculadora;

    public MotorRecomendacion(ParametrosMotor parametros, CalculadoraRecuperacion calculadora) {
        this.parametros = parametros;
        this.calculadora = calculadora;
    }

    /** Cuantos dias de historial necesita el motor (la ventana de analisis por dos). */
    public int diasHistorialRequeridos() {
        return parametros.diasVentana() * 2;
    }

    /**
     * Evalua el estado de todas las microzonas a partir del historial de aplicaciones.
     *
     * @param mapa      mapa corporal del paciente
     * @param historial aplicaciones no deshechas (cualquier orden)
     * @param ahora     instante de evaluacion
     */
    public ResultadoEvaluacion evaluar(MapaCorporal mapa, List<Aplicacion> historial, Instant ahora) {
        Instant inicioVentana = ahora.minus(Duration.ofDays(parametros.diasVentana()));
        double descansoMaximo = diasHistorialRequeridos() * HORAS_POR_DIA;

        // 1. Ultima aplicacion directa y frecuencia de uso por microzona (tabla hash O(1)).
        Map<String, Instant> ultimaPorMicrozona = new HashMap<>();
        Map<String, Integer> usosPorMicrozona = new HashMap<>();
        Map<ZonaMacro, Integer> usosPorZona = new EnumMap<>(ZonaMacro.class);
        Map<ZonaMacro, Instant> ultimaPorZona = new EnumMap<>(ZonaMacro.class);
        for (Aplicacion aplicacion : historial) {
            if (aplicacion.isDeshecha() || aplicacion.getFechaHora().isAfter(ahora)) {
                continue;
            }
            Microzona microzona = mapa.buscar(aplicacion.getMicrozonaId()).orElse(null);
            if (microzona == null) {
                continue; // microzona fuera de la cuadricula actual (cambio de configuracion)
            }
            ultimaPorMicrozona.merge(microzona.id(), aplicacion.getFechaHora(), MotorRecomendacion::masReciente);
            ultimaPorZona.merge(microzona.zonaMacro(), aplicacion.getFechaHora(), MotorRecomendacion::masReciente);
            if (!aplicacion.getFechaHora().isBefore(inicioVentana)) {
                usosPorMicrozona.merge(microzona.id(), 1, Integer::sum);
                usosPorZona.merge(microzona.zonaMacro(), 1, Integer::sum);
            }
        }

        // 2. Bloqueo por adyacencia (RF07): se recorre el grafo desde cada microzona usada.
        Map<String, BloqueoAdyacente> bloqueos = new HashMap<>();
        ultimaPorMicrozona.forEach((origenId, fecha) -> {
            for (String vecinoId : mapa.vecinos(origenId)) {
                Microzona vecino = mapa.buscar(vecinoId).orElseThrow();
                double horas = calculadora.horasBloqueoAdyacente(
                        vecino.zonaMacro(), usosPorMicrozona.getOrDefault(vecinoId, 0));
                double progreso = calculadora.progreso(horasEntre(fecha, ahora), horas);
                BloqueoAdyacente actual = bloqueos.get(vecinoId);
                if (actual == null || progreso < actual.progreso()) {
                    bloqueos.put(vecinoId, new BloqueoAdyacente(origenId, progreso, fecha.plus(horasADuracion(horas))));
                }
            }
        });

        // 3. Estado final y semaforo de cada microzona.
        MapaEstados estados = new MapaEstados();
        for (Microzona microzona : mapa.todas()) {
            int usos = usosPorMicrozona.getOrDefault(microzona.id(), 0);
            double horasRequeridas = calculadora.horasRecuperacion(microzona.zonaMacro(), usos);
            Instant ultima = ultimaPorMicrozona.get(microzona.id());

            double horasDescanso = ultima == null ? descansoMaximo : Math.min(descansoMaximo, horasEntre(ultima, ahora));
            double progresoDirecto = ultima == null ? 1.0 : calculadora.progreso(horasDescanso, horasRequeridas);
            Instant finDirecto = ultima == null ? null : ultima.plus(horasADuracion(horasRequeridas));

            BloqueoAdyacente bloqueo = bloqueos.get(microzona.id());
            boolean porAdyacencia = bloqueo != null && bloqueo.progreso() < Math.min(1.0, progresoDirecto);
            double progreso = porAdyacencia ? bloqueo.progreso() : progresoDirecto;
            Semaforo semaforo = calculadora.semaforo(progreso);

            Instant disponibleDesde = null;
            if (semaforo != Semaforo.VERDE) {
                disponibleDesde = finDirecto;
                if (bloqueo != null && (disponibleDesde == null || bloqueo.fin().isAfter(disponibleDesde))) {
                    disponibleDesde = bloqueo.fin();
                }
            }
            estados.actualizar(new EstadoMicrozona(microzona, semaforo, Math.min(1.0, progreso), horasDescanso,
                    horasRequeridas, usos, ultima, disponibleDesde, porAdyacencia,
                    porAdyacencia ? bloqueo.origenId() : null));
        }

        // 4. Resumen por zona macro: saturadas y olvidadas (RF09).
        int totalUsos = usosPorZona.values().stream().mapToInt(Integer::intValue).sum();
        double promedio = (double) totalUsos / ZonaMacro.values().length;
        Map<ZonaMacro, ResumenZonaMacro> resumen = new EnumMap<>(ZonaMacro.class);
        for (ZonaMacro zona : ZonaMacro.values()) {
            int usos = usosPorZona.getOrDefault(zona, 0);
            Instant ultima = ultimaPorZona.get(zona);
            double diasSinUso = ultima == null ? -1 : horasEntre(ultima, ahora) / HORAS_POR_DIA;
            boolean saturada = totalUsos > 0 && usos > promedio;
            boolean olvidada = ultima == null || diasSinUso >= parametros.diasZonaOlvidada();
            double factor = (saturada ? parametros.penalizacionZonaSaturada() : 1.0)
                    * (olvidada ? parametros.bonificacionZonaOlvidada() : 1.0);
            resumen.put(zona, new ResumenZonaMacro(zona, usos, diasSinUso, saturada, olvidada, factor));
        }
        return new ResultadoEvaluacion(mapa, estados, resumen, promedio, ahora);
    }

    /**
     * Sugiere la microzona optima. Todas las microzonas se insertan en un Max-Heap ordenado por
     * (nivel de semaforo, puntaje) y se extrae la raiz en O(log n).
     *
     * <p>puntaje = horasDescanso x factorPrioridad(zona) para microzonas VERDES;
     * para las no recuperadas se usa progreso x factorPrioridad.</p>
     */
    public Recomendacion recomendar(ResultadoEvaluacion evaluacion) {
        long inicio = System.nanoTime();
        Comparator<CandidatoRecomendacion> orden = Comparator
                .comparingInt(CandidatoRecomendacion::nivel)
                .thenComparingDouble(CandidatoRecomendacion::puntaje)
                .thenComparing(c -> c.estado().getMicrozona().id(), Comparator.reverseOrder());
        MonticuloMaximo<CandidatoRecomendacion> monticulo = new MonticuloMaximo<>(orden);

        for (EstadoMicrozona estado : evaluacion.estados().todos()) {
            double factor = evaluacion.resumenZonas().get(estado.getMicrozona().zonaMacro()).factorPrioridad();
            double base = estado.estaDisponible() ? estado.getHorasDescanso() : estado.getProgresoRecuperacion();
            monticulo.insertar(new CandidatoRecomendacion(estado, base * factor));
        }

        CandidatoRecomendacion principal = monticulo.extraerMaximo();
        List<CandidatoRecomendacion> alternativas = new ArrayList<>();
        while (!monticulo.estaVacio() && alternativas.size() < parametros.alternativas()) {
            alternativas.add(monticulo.extraerMaximo());
        }

        List<ZonaMacro> saturadas = new ArrayList<>();
        List<ZonaMacro> olvidadas = new ArrayList<>();
        evaluacion.resumenZonas().values().forEach(r -> {
            if (r.saturada()) {
                saturadas.add(r.zona());
            }
            if (r.olvidada()) {
                olvidadas.add(r.zona());
            }
        });
        boolean hayOptimas = principal.estado().estaDisponible();
        double milisegundos = (System.nanoTime() - inicio) / 1_000_000.0;
        return new Recomendacion(principal, alternativas, saturadas, olvidadas, hayOptimas,
                construirMotivo(principal, evaluacion, hayOptimas), milisegundos);
    }

    private String construirMotivo(CandidatoRecomendacion candidato, ResultadoEvaluacion evaluacion,
            boolean hayOptimas) {
        EstadoMicrozona estado = candidato.estado();
        ResumenZonaMacro resumen = evaluacion.resumenZonas().get(estado.getMicrozona().zonaMacro());
        StringBuilder motivo = new StringBuilder();
        if (!hayOptimas) {
            motivo.append("No hay microzonas totalmente recuperadas; se sugiere la de mayor recuperacion (")
                    .append(Math.round(estado.getProgresoRecuperacion() * 100)).append("%). ");
        }
        motivo.append(String.format(Locale.ROOT, "%s (%s) acumula %.1f h de descanso",
                estado.getMicrozona().id(), estado.getMicrozona().zonaMacro().getNombre(), estado.getHorasDescanso()));
        if (estado.getUltimaAplicacion() == null) {
            motivo.append(" y no registra aplicaciones recientes");
        }
        motivo.append('.');
        if (resumen.olvidada()) {
            motivo.append(" Zona olvidada: se prioriza.");
        }
        if (resumen.saturada()) {
            motivo.append(" Zona saturada: se penaliza, pero sigue siendo la mejor opcion.");
        }
        return motivo.toString();
    }

    private static Instant masReciente(Instant a, Instant b) {
        return a.isAfter(b) ? a : b;
    }

    private static double horasEntre(Instant desde, Instant hasta) {
        return Duration.between(desde, hasta).toMillis() / 3_600_000.0;
    }

    private static Duration horasADuracion(double horas) {
        return Duration.ofMillis(Math.round(horas * 3_600_000));
    }

    private record BloqueoAdyacente(String origenId, double progreso, Instant fin) {
    }
}
