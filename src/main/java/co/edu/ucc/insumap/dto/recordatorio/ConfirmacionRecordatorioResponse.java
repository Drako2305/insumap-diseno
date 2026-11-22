package co.edu.ucc.insumap.dto.recordatorio;

import co.edu.ucc.insumap.dto.aplicacion.RegistroAplicacionResponse;

/** @param registro registro de la inyeccion, presente solo si se envio una microzona al confirmar */
public record ConfirmacionRecordatorioResponse(RecordatorioResponse recordatorio, RegistroAplicacionResponse registro) {
}
