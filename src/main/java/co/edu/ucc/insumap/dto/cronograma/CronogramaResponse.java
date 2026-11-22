package co.edu.ucc.insumap.dto.cronograma;

import co.edu.ucc.insumap.modelo.CronogramaDosis;
import co.edu.ucc.insumap.modelo.enums.TipoInsulina;
import java.time.DayOfWeek;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public record CronogramaResponse(
        String id,
        String pacienteId,
        String nombre,
        TipoInsulina tipoInsulina,
        double unidades,
        List<String> horas,
        Set<DayOfWeek> diasSemana,
        boolean activo,
        Instant fechaCreacion) {

    public static CronogramaResponse desde(CronogramaDosis c) {
        return new CronogramaResponse(c.getId(), c.getPacienteId(), c.getNombre(), c.getTipoInsulina(),
                c.getUnidades(), c.getHoras(), new TreeSet<>(c.getDiasSemana()), c.isActivo(), c.getFechaCreacion());
    }
}
