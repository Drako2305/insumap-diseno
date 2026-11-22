package co.edu.ucc.insumap.dto.mapa;

import co.edu.ucc.insumap.modelo.enums.ZonaMacro;
import java.util.List;

/** Indicadores de rotacion para el seguimiento medico (RF09, RF11). */
public record EstadisticasResponse(
        String pacienteId,
        int diasVentana,
        int aplicacionesEnVentana,
        double promedioPorZona,
        double porcentajeAdherencia,
        int aplicacionesConAdvertencia,
        ResumenSemaforoResponse semaforo,
        List<UsoZonaResponse> zonas) {

    public record UsoZonaResponse(
            ZonaMacro zona,
            int usos,
            double porcentaje,
            double diasSinUso,
            boolean saturada,
            boolean olvidada) {
    }
}
