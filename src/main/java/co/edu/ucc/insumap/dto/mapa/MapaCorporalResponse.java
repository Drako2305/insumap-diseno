package co.edu.ucc.insumap.dto.mapa;

import java.time.Instant;
import java.util.List;

/** Mapa corporal 2D completo con el semaforo de cada microzona (RF01, RF02, RF06). */
public record MapaCorporalResponse(
        String pacienteId,
        int filas,
        int columnas,
        Instant evaluadoEn,
        ResumenSemaforoResponse resumenSemaforo,
        RecomendacionResponse recomendacion,
        List<ZonaMacroResponse> zonas) {
}
