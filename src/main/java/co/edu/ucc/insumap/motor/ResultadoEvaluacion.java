package co.edu.ucc.insumap.motor;

import co.edu.ucc.insumap.modelo.enums.ZonaMacro;
import co.edu.ucc.insumap.motor.estructuras.MapaEstados;
import java.time.Instant;
import java.util.Map;

/**
 * Resultado de evaluar el historial de un paciente: el estado de cada microzona (tabla hash) y el
 * resumen de uso por zona macro.
 */
public record ResultadoEvaluacion(
        MapaCorporal mapa,
        MapaEstados estados,
        Map<ZonaMacro, ResumenZonaMacro> resumenZonas,
        double promedioUsosPorZona,
        Instant evaluadoEn) {
}
