package co.edu.ucc.insumap.dto.recordatorio;

import java.time.Instant;
import java.util.List;

/** Resultado de un ciclo del planificador: recordatorios generados y notificaciones despachadas (FIFO). */
public record CicloRecordatoriosResponse(Instant ejecutadoEn, int generados, List<RecordatorioResponse> despachados) {
}
