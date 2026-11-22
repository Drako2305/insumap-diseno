package co.edu.ucc.insumap.dto.recordatorio;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** @param minutos minutos de aplazamiento; si se omite se usa el valor configurado (15). */
public record AplazarRecordatorioRequest(@Min(1) @Max(240) Integer minutos) {
}
