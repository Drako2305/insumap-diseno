package co.edu.ucc.insumap.dto.aplicacion;

import co.edu.ucc.insumap.dto.mapa.MicrozonaEstadoResponse;
import co.edu.ucc.insumap.modelo.enums.Semaforo;
import java.util.List;

/**
 * Resultado de validar una microzona antes de registrar (RF04). El registro siempre esta permitido:
 * si la microzona no esta recuperada se devuelve una advertencia no bloqueante.
 */
public record ValidacionAplicacionResponse(
        String microzonaId,
        Semaforo semaforo,
        boolean permitido,
        boolean requiereAdvertencia,
        String advertencia,
        List<String> microzonasQueSeBloquearan,
        MicrozonaEstadoResponse estado,
        MicrozonaEstadoResponse sugerencia) {
}
