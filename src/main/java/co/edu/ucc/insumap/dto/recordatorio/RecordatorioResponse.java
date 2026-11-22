package co.edu.ucc.insumap.dto.recordatorio;

import co.edu.ucc.insumap.modelo.Recordatorio;
import co.edu.ucc.insumap.modelo.enums.EstadoRecordatorio;
import co.edu.ucc.insumap.modelo.enums.TipoInsulina;
import java.time.Instant;

public record RecordatorioResponse(
        String id,
        String pacienteId,
        String cronogramaId,
        TipoInsulina tipoInsulina,
        double unidades,
        Instant fechaOriginal,
        Instant fechaProgramada,
        Instant fechaEnvio,
        Instant fechaRespuesta,
        EstadoRecordatorio estado,
        int aplazamientos,
        String aplicacionId) {

    public static RecordatorioResponse desde(Recordatorio r) {
        return new RecordatorioResponse(r.getId(), r.getPacienteId(), r.getCronogramaId(), r.getTipoInsulina(),
                r.getUnidades(), r.getFechaOriginal(), r.getFechaProgramada(), r.getFechaEnvio(),
                r.getFechaRespuesta(), r.getEstado(), r.getAplazamientos(), r.getAplicacionId());
    }
}
