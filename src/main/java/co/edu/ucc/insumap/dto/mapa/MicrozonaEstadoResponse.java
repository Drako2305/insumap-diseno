package co.edu.ucc.insumap.dto.mapa;

import co.edu.ucc.insumap.modelo.enums.Lado;
import co.edu.ucc.insumap.modelo.enums.Semaforo;
import co.edu.ucc.insumap.modelo.enums.ZonaMacro;
import co.edu.ucc.insumap.motor.EstadoMicrozona;
import java.time.Instant;

/** Estado de una microzona listo para pintarse en el mapa corporal (RF06). */
public record MicrozonaEstadoResponse(
        String id,
        ZonaMacro zonaMacro,
        Lado lado,
        int fila,
        int columna,
        Semaforo semaforo,
        int porcentajeRecuperacion,
        double horasDescanso,
        double horasRecuperacionRequeridas,
        int usosEnVentana,
        Instant ultimaAplicacion,
        Instant disponibleDesde,
        boolean bloqueadaPorAdyacencia,
        String origenBloqueo) {

    public static MicrozonaEstadoResponse desde(EstadoMicrozona e) {
        return new MicrozonaEstadoResponse(e.getMicrozona().id(), e.getMicrozona().zonaMacro(),
                e.getMicrozona().lado(), e.getMicrozona().fila(), e.getMicrozona().columna(), e.getSemaforo(),
                (int) Math.round(e.getProgresoRecuperacion() * 100), redondear(e.getHorasDescanso()),
                redondear(e.getHorasRecuperacionRequeridas()), e.getUsosEnVentana(), e.getUltimaAplicacion(),
                e.getDisponibleDesde(), e.isBloqueadaPorAdyacencia(), e.getOrigenBloqueo());
    }

    public static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
