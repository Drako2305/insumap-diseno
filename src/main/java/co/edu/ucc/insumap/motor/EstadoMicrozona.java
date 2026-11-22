package co.edu.ucc.insumap.motor;

import co.edu.ucc.insumap.modelo.enums.Semaforo;
import co.edu.ucc.insumap.motor.estructuras.Microzona;
import java.time.Instant;

/** Estado de recuperacion calculado para una microzona en un instante dado (RF06, RF08). */
public class EstadoMicrozona {

    private final Microzona microzona;
    private final Semaforo semaforo;
    /** Fraccion de recuperacion entre 0 y 1 (1 = recuperada). */
    private final double progresoRecuperacion;
    /** Horas acumuladas desde la ultima aplicacion directa en la microzona. */
    private final double horasDescanso;
    /** Horas de recuperacion biologica requeridas segun zona y frecuencia de uso. */
    private final double horasRecuperacionRequeridas;
    private final int usosEnVentana;
    private final Instant ultimaAplicacion;
    /** Momento en que la microzona vuelve a estar VERDE (null si ya lo esta). */
    private final Instant disponibleDesde;
    private final boolean bloqueadaPorAdyacencia;
    /** Microzona cuya aplicacion origino el bloqueo por adyacencia (RF07). */
    private final String origenBloqueo;

    public EstadoMicrozona(Microzona microzona, Semaforo semaforo, double progresoRecuperacion,
            double horasDescanso, double horasRecuperacionRequeridas, int usosEnVentana,
            Instant ultimaAplicacion, Instant disponibleDesde, boolean bloqueadaPorAdyacencia,
            String origenBloqueo) {
        this.microzona = microzona;
        this.semaforo = semaforo;
        this.progresoRecuperacion = progresoRecuperacion;
        this.horasDescanso = horasDescanso;
        this.horasRecuperacionRequeridas = horasRecuperacionRequeridas;
        this.usosEnVentana = usosEnVentana;
        this.ultimaAplicacion = ultimaAplicacion;
        this.disponibleDesde = disponibleDesde;
        this.bloqueadaPorAdyacencia = bloqueadaPorAdyacencia;
        this.origenBloqueo = origenBloqueo;
    }

    public boolean estaDisponible() {
        return semaforo == Semaforo.VERDE;
    }

    public Microzona getMicrozona() {
        return microzona;
    }

    public Semaforo getSemaforo() {
        return semaforo;
    }

    public double getProgresoRecuperacion() {
        return progresoRecuperacion;
    }

    public double getHorasDescanso() {
        return horasDescanso;
    }

    public double getHorasRecuperacionRequeridas() {
        return horasRecuperacionRequeridas;
    }

    public int getUsosEnVentana() {
        return usosEnVentana;
    }

    public Instant getUltimaAplicacion() {
        return ultimaAplicacion;
    }

    public Instant getDisponibleDesde() {
        return disponibleDesde;
    }

    public boolean isBloqueadaPorAdyacencia() {
        return bloqueadaPorAdyacencia;
    }

    public String getOrigenBloqueo() {
        return origenBloqueo;
    }
}
