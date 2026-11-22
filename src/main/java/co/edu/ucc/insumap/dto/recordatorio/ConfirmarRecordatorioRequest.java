package co.edu.ucc.insumap.dto.recordatorio;

import co.edu.ucc.insumap.modelo.enums.RegistradoPor;
import jakarta.validation.constraints.Size;

/**
 * Confirmacion de una dosis recordada. Si se envia microzonaId, ademas se registra la inyeccion con el
 * tipo de insulina y las unidades del cronograma.
 */
public record ConfirmarRecordatorioRequest(
        @Size(max = 20) String microzonaId,
        RegistradoPor registradoPor,
        @Size(max = 500) String notas) {
}
