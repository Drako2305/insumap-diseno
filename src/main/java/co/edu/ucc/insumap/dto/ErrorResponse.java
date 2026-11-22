package co.edu.ucc.insumap.dto;

import java.time.Instant;
import java.util.List;

/** Formato uniforme de error que devuelve la API. */
public record ErrorResponse(
        Instant timestamp,
        int estado,
        String error,
        String mensaje,
        String ruta,
        List<String> detalles) {
}
