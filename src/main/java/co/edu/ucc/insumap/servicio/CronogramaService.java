package co.edu.ucc.insumap.servicio;

import co.edu.ucc.insumap.dto.cronograma.CronogramaRequest;
import co.edu.ucc.insumap.dto.cronograma.CronogramaResponse;
import co.edu.ucc.insumap.excepcion.RecursoNoEncontradoException;
import co.edu.ucc.insumap.modelo.CronogramaDosis;
import co.edu.ucc.insumap.modelo.enums.EstadoRecordatorio;
import co.edu.ucc.insumap.repositorio.CronogramaRepository;
import co.edu.ucc.insumap.repositorio.RecordatorioRepository;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import org.springframework.stereotype.Service;

/** Configuracion del cronograma de dosis del paciente (RF10). */
@Service
public class CronogramaService {

    private static final List<EstadoRecordatorio> ESTADOS_ABIERTOS =
            List.of(EstadoRecordatorio.PENDIENTE, EstadoRecordatorio.APLAZADO);

    private final PacienteService pacienteService;
    private final CronogramaRepository cronogramaRepository;
    private final RecordatorioRepository recordatorioRepository;
    private final Clock reloj;

    public CronogramaService(PacienteService pacienteService, CronogramaRepository cronogramaRepository,
            RecordatorioRepository recordatorioRepository, Clock reloj) {
        this.pacienteService = pacienteService;
        this.cronogramaRepository = cronogramaRepository;
        this.recordatorioRepository = recordatorioRepository;
        this.reloj = reloj;
    }

    public CronogramaResponse crear(String pacienteId, CronogramaRequest solicitud) {
        pacienteService.obtener(pacienteId);
        CronogramaDosis cronograma = new CronogramaDosis();
        cronograma.setPacienteId(pacienteId);
        cronograma.setFechaCreacion(Instant.now(reloj));
        aplicar(cronograma, solicitud);
        return CronogramaResponse.desde(cronogramaRepository.save(cronograma));
    }

    public List<CronogramaResponse> listar(String pacienteId) {
        pacienteService.obtener(pacienteId);
        return cronogramaRepository.findByPacienteId(pacienteId).stream().map(CronogramaResponse::desde).toList();
    }

    public CronogramaResponse consultar(String pacienteId, String cronogramaId) {
        return CronogramaResponse.desde(obtener(pacienteId, cronogramaId));
    }

    public CronogramaResponse actualizar(String pacienteId, String cronogramaId, CronogramaRequest solicitud) {
        CronogramaDosis cronograma = obtener(pacienteId, cronogramaId);
        aplicar(cronograma, solicitud);
        // Los recordatorios pendientes se regeneran con la nueva configuracion en el siguiente ciclo.
        recordatorioRepository.deleteByCronogramaIdAndEstadoIn(cronogramaId, ESTADOS_ABIERTOS);
        return CronogramaResponse.desde(cronogramaRepository.save(cronograma));
    }

    public CronogramaResponse cambiarEstado(String pacienteId, String cronogramaId, boolean activo) {
        CronogramaDosis cronograma = obtener(pacienteId, cronogramaId);
        cronograma.setActivo(activo);
        if (!activo) {
            recordatorioRepository.deleteByCronogramaIdAndEstadoIn(cronogramaId, ESTADOS_ABIERTOS);
        }
        return CronogramaResponse.desde(cronogramaRepository.save(cronograma));
    }

    public void eliminar(String pacienteId, String cronogramaId) {
        CronogramaDosis cronograma = obtener(pacienteId, cronogramaId);
        recordatorioRepository.deleteByCronogramaIdAndEstadoIn(cronogramaId, ESTADOS_ABIERTOS);
        cronogramaRepository.delete(cronograma);
    }

    private CronogramaDosis obtener(String pacienteId, String cronogramaId) {
        return cronogramaRepository.findById(cronogramaId)
                .filter(c -> c.getPacienteId().equals(pacienteId))
                .orElseThrow(() -> new RecursoNoEncontradoException("Cronograma", cronogramaId));
    }

    private void aplicar(CronogramaDosis cronograma, CronogramaRequest solicitud) {
        cronograma.setNombre(solicitud.nombre().trim());
        cronograma.setTipoInsulina(solicitud.tipoInsulina());
        cronograma.setUnidades(solicitud.unidades());
        cronograma.setHoras(solicitud.horas().stream().distinct().sorted().toList());
        cronograma.setDiasSemana(solicitud.diasSemana() == null || solicitud.diasSemana().isEmpty()
                ? EnumSet.allOf(DayOfWeek.class) : EnumSet.copyOf(solicitud.diasSemana()));
        if (solicitud.activo() != null) {
            cronograma.setActivo(solicitud.activo());
        }
    }
}
