package co.edu.ucc.insumap.dto.aplicacion;

import co.edu.ucc.insumap.modelo.Aplicacion;
import co.edu.ucc.insumap.modelo.enums.Lado;
import co.edu.ucc.insumap.modelo.enums.RegistradoPor;
import co.edu.ucc.insumap.modelo.enums.Semaforo;
import co.edu.ucc.insumap.modelo.enums.TipoInsulina;
import co.edu.ucc.insumap.modelo.enums.ZonaMacro;
import java.time.Instant;

public record AplicacionResponse(
        String id,
        String pacienteId,
        String microzonaId,
        ZonaMacro zonaMacro,
        Lado lado,
        int fila,
        int columna,
        TipoInsulina tipoInsulina,
        double unidades,
        Instant fechaHora,
        RegistradoPor registradoPor,
        String notas,
        Semaforo semaforoAlRegistrar,
        boolean enZonaRecomendada,
        boolean deshecha,
        Instant fechaDeshecha,
        String recordatorioId) {

    public static AplicacionResponse desde(Aplicacion a) {
        return new AplicacionResponse(a.getId(), a.getPacienteId(), a.getMicrozonaId(), a.getZonaMacro(), a.getLado(),
                a.getFila(), a.getColumna(), a.getTipoInsulina(), a.getUnidades(), a.getFechaHora(),
                a.getRegistradoPor(), a.getNotas(), a.getSemaforoAlRegistrar(), a.isEnZonaRecomendada(),
                a.isDeshecha(), a.getFechaDeshecha(), a.getRecordatorioId());
    }
}
