package co.edu.ucc.insumap.servicio;

import co.edu.ucc.insumap.config.ParametrosRecordatorios;
import co.edu.ucc.insumap.dto.aplicacion.RegistrarAplicacionRequest;
import co.edu.ucc.insumap.dto.aplicacion.RegistroAplicacionResponse;
import co.edu.ucc.insumap.dto.recordatorio.CicloRecordatoriosResponse;
import co.edu.ucc.insumap.dto.recordatorio.ConfirmacionRecordatorioResponse;
import co.edu.ucc.insumap.dto.recordatorio.ConfirmarRecordatorioRequest;
import co.edu.ucc.insumap.dto.recordatorio.RecordatorioResponse;
import co.edu.ucc.insumap.excepcion.RecursoNoEncontradoException;
import co.edu.ucc.insumap.excepcion.ReglaNegocioException;
import co.edu.ucc.insumap.modelo.CronogramaDosis;
import co.edu.ucc.insumap.modelo.Recordatorio;
import co.edu.ucc.insumap.modelo.enums.EstadoRecordatorio;
import co.edu.ucc.insumap.motor.estructuras.ColaAlertas;
import co.edu.ucc.insumap.repositorio.CronogramaRepository;
import co.edu.ucc.insumap.repositorio.RecordatorioRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * Genera, despacha y gestiona los recordatorios de dosis (RF10).
 * El despacho usa una cola FIFO: las alertas se notifican en orden cronologico.
 */
@Service
public class RecordatorioService {

    private static final Logger LOG = LoggerFactory.getLogger(RecordatorioService.class);
    private static final Duration HORIZONTE_GENERACION = Duration.ofHours(24);
    private static final Duration TOLERANCIA_PASADO = Duration.ofHours(1);
    private static final List<EstadoRecordatorio> ESTADOS_POR_DESPACHAR =
            List.of(EstadoRecordatorio.PENDIENTE, EstadoRecordatorio.APLAZADO);

    private final RecordatorioRepository recordatorioRepository;
    private final CronogramaRepository cronogramaRepository;
    private final PacienteService pacienteService;
    private final AplicacionService aplicacionService;
    private final ParametrosRecordatorios parametros;
    private final ZoneId zona;
    private final Clock reloj;
    private final ColaAlertas<Recordatorio> cola = new ColaAlertas<>();

    public RecordatorioService(RecordatorioRepository recordatorioRepository, CronogramaRepository cronogramaRepository,
            PacienteService pacienteService, AplicacionService aplicacionService, ParametrosRecordatorios parametros,
            Clock reloj) {
        this.recordatorioRepository = recordatorioRepository;
        this.cronogramaRepository = cronogramaRepository;
        this.pacienteService = pacienteService;
        this.aplicacionService = aplicacionService;
        this.parametros = parametros;
        this.zona = ZoneId.of(parametros.zonaHoraria());
        this.reloj = reloj;
    }

    /** Ciclo completo del planificador: generar las dosis proximas y despachar las vencidas. */
    public CicloRecordatoriosResponse ejecutarCiclo() {
        Instant ahora = Instant.now(reloj);
        int generados = generar(ahora);
        List<RecordatorioResponse> despachados = despachar(ahora).stream().map(RecordatorioResponse::desde).toList();
        return new CicloRecordatoriosResponse(ahora, generados, despachados);
    }

    /** Crea los recordatorios de las proximas 24 horas a partir de los cronogramas activos (idempotente). */
    int generar(Instant ahora) {
        int generados = 0;
        LocalDate hoy = LocalDate.ofInstant(ahora, zona);
        for (CronogramaDosis cronograma : cronogramaRepository.findByActivoTrue()) {
            for (LocalDate dia : List.of(hoy, hoy.plusDays(1))) {
                if (!cronograma.aplicaEnDia(dia.getDayOfWeek())) {
                    continue;
                }
                for (String hora : cronograma.getHoras()) {
                    Instant programada = dia.atTime(LocalTime.parse(hora)).atZone(zona).toInstant();
                    boolean enHorizonte = programada.isAfter(ahora.minus(TOLERANCIA_PASADO))
                            && !programada.isAfter(ahora.plus(HORIZONTE_GENERACION))
                            && !programada.isBefore(cronograma.getFechaCreacion());
                    if (enHorizonte && !recordatorioRepository.existsByCronogramaIdAndFechaOriginal(
                            cronograma.getId(), programada)) {
                        generados += crear(cronograma, programada) ? 1 : 0;
                    }
                }
            }
        }
        return generados;
    }

    private boolean crear(CronogramaDosis cronograma, Instant programada) {
        Recordatorio recordatorio = new Recordatorio();
        recordatorio.setPacienteId(cronograma.getPacienteId());
        recordatorio.setCronogramaId(cronograma.getId());
        recordatorio.setTipoInsulina(cronograma.getTipoInsulina());
        recordatorio.setUnidades(cronograma.getUnidades());
        recordatorio.setFechaOriginal(programada);
        recordatorio.setFechaProgramada(programada);
        try {
            recordatorioRepository.save(recordatorio);
            return true;
        } catch (DuplicateKeyException e) {
            return false; // otra instancia ya lo genero
        }
    }

    /** Encola en orden cronologico los recordatorios vencidos y los despacha (FIFO). */
    List<Recordatorio> despachar(Instant ahora) {
        recordatorioRepository
                .findByEstadoInAndFechaProgramadaLessThanEqualOrderByFechaProgramadaAsc(ESTADOS_POR_DESPACHAR, ahora)
                .forEach(cola::encolar);
        List<Recordatorio> despachados = new ArrayList<>();
        while (!cola.estaVacia()) {
            Recordatorio recordatorio = cola.desencolar().orElseThrow();
            recordatorio.setEstado(EstadoRecordatorio.ENVIADO);
            recordatorio.setFechaEnvio(ahora);
            despachados.add(recordatorioRepository.save(recordatorio));
            LOG.info("[NOTIFICACION] Paciente {}: aplicar {} U de insulina {} (programada {})",
                    recordatorio.getPacienteId(), recordatorio.getUnidades(), recordatorio.getTipoInsulina(),
                    recordatorio.getFechaProgramada());
        }
        return despachados;
    }

    public List<RecordatorioResponse> listar(String pacienteId, EstadoRecordatorio estado) {
        pacienteService.obtener(pacienteId);
        List<Recordatorio> recordatorios = estado == null
                ? recordatorioRepository.findByPacienteIdOrderByFechaProgramadaDesc(pacienteId)
                : recordatorioRepository.findByPacienteIdAndEstadoOrderByFechaProgramadaDesc(pacienteId, estado);
        return recordatorios.stream().map(RecordatorioResponse::desde).toList();
    }

    public RecordatorioResponse consultar(String recordatorioId) {
        return RecordatorioResponse.desde(obtener(recordatorioId));
    }

    /** Confirma la dosis; si se indica la microzona, registra tambien la inyeccion. */
    public ConfirmacionRecordatorioResponse confirmar(String recordatorioId, ConfirmarRecordatorioRequest solicitud) {
        Recordatorio recordatorio = obtenerAbierto(recordatorioId);
        RegistroAplicacionResponse registro = null;
        if (solicitud != null && solicitud.microzonaId() != null && !solicitud.microzonaId().isBlank()) {
            registro = aplicacionService.registrar(recordatorio.getPacienteId(),
                    new RegistrarAplicacionRequest(solicitud.microzonaId(), recordatorio.getTipoInsulina(),
                            recordatorio.getUnidades(), null, solicitud.registradoPor(), solicitud.notas()),
                    recordatorio.getId());
            recordatorio.setAplicacionId(registro.aplicacion().id());
        }
        recordatorio.setEstado(EstadoRecordatorio.CONFIRMADO);
        recordatorio.setFechaRespuesta(Instant.now(reloj));
        return new ConfirmacionRecordatorioResponse(
                RecordatorioResponse.desde(recordatorioRepository.save(recordatorio)), registro);
    }

    /** Aplaza el recordatorio unos minutos; vuelve a la cola cuando venza (maximo configurable). */
    public RecordatorioResponse aplazar(String recordatorioId, Integer minutos) {
        Recordatorio recordatorio = obtenerAbierto(recordatorioId);
        if (!recordatorio.puedeAplazarse(parametros.maximoAplazamientos())) {
            throw new ReglaNegocioException("El recordatorio alcanzo el maximo de "
                    + parametros.maximoAplazamientos() + " aplazamientos");
        }
        int espera = minutos == null ? parametros.minutosAplazamiento() : minutos;
        recordatorio.setFechaProgramada(Instant.now(reloj).plus(Duration.ofMinutes(espera)));
        recordatorio.setAplazamientos(recordatorio.getAplazamientos() + 1);
        recordatorio.setEstado(EstadoRecordatorio.APLAZADO);
        return RecordatorioResponse.desde(recordatorioRepository.save(recordatorio));
    }

    public RecordatorioResponse omitir(String recordatorioId) {
        Recordatorio recordatorio = obtenerAbierto(recordatorioId);
        recordatorio.setEstado(EstadoRecordatorio.OMITIDO);
        recordatorio.setFechaRespuesta(Instant.now(reloj));
        return RecordatorioResponse.desde(recordatorioRepository.save(recordatorio));
    }

    private Recordatorio obtener(String recordatorioId) {
        return recordatorioRepository.findById(recordatorioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Recordatorio", recordatorioId));
    }

    private Recordatorio obtenerAbierto(String recordatorioId) {
        Recordatorio recordatorio = obtener(recordatorioId);
        if (!recordatorio.estaAbierto()) {
            throw new ReglaNegocioException("El recordatorio ya fue respondido (" + recordatorio.getEstado() + ")");
        }
        return recordatorio;
    }
}
