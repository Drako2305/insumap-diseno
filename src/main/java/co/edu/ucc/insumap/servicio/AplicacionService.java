package co.edu.ucc.insumap.servicio;

import co.edu.ucc.insumap.dto.aplicacion.AplicacionResponse;
import co.edu.ucc.insumap.dto.aplicacion.DeshacerResponse;
import co.edu.ucc.insumap.dto.aplicacion.RegistrarAplicacionRequest;
import co.edu.ucc.insumap.dto.aplicacion.RegistroAplicacionResponse;
import co.edu.ucc.insumap.dto.aplicacion.ValidacionAplicacionResponse;
import co.edu.ucc.insumap.dto.mapa.MicrozonaEstadoResponse;
import co.edu.ucc.insumap.excepcion.RecursoNoEncontradoException;
import co.edu.ucc.insumap.excepcion.ReglaNegocioException;
import co.edu.ucc.insumap.excepcion.SolicitudInvalidaException;
import co.edu.ucc.insumap.modelo.Aplicacion;
import co.edu.ucc.insumap.modelo.Paciente;
import co.edu.ucc.insumap.modelo.enums.RegistradoPor;
import co.edu.ucc.insumap.modelo.enums.Semaforo;
import co.edu.ucc.insumap.modelo.enums.ZonaMacro;
import co.edu.ucc.insumap.motor.EstadoMicrozona;
import co.edu.ucc.insumap.motor.MapaCorporal;
import co.edu.ucc.insumap.motor.Recomendacion;
import co.edu.ucc.insumap.motor.ResultadoEvaluacion;
import co.edu.ucc.insumap.motor.estructuras.Microzona;
import co.edu.ucc.insumap.motor.estructuras.PilaDeshacer;
import co.edu.ucc.insumap.repositorio.AplicacionRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/** Registro, validacion, deshacer e historial de aplicaciones de insulina (RF03 - RF05, RF07, RF11). */
@Service
public class AplicacionService {

    /** Tolerancia para fechas levemente adelantadas por diferencias de reloj del dispositivo. */
    private static final Duration TOLERANCIA_FUTURO = Duration.ofMinutes(5);
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("dd/MM HH:mm")
            .withZone(ZoneId.of("America/Bogota"));

    private final PacienteService pacienteService;
    private final MapaCorporalService mapaCorporalService;
    private final AplicacionRepository aplicacionRepository;
    private final Clock reloj;

    public AplicacionService(PacienteService pacienteService, MapaCorporalService mapaCorporalService,
            AplicacionRepository aplicacionRepository, Clock reloj) {
        this.pacienteService = pacienteService;
        this.mapaCorporalService = mapaCorporalService;
        this.aplicacionRepository = aplicacionRepository;
        this.reloj = reloj;
    }

    /** Valida la microzona antes de registrar: nunca bloquea, solo advierte (RF04). */
    public ValidacionAplicacionResponse validar(String pacienteId, String microzonaId) {
        Paciente paciente = pacienteService.obtener(pacienteId);
        ResultadoEvaluacion evaluacion = mapaCorporalService.evaluar(paciente);
        EstadoMicrozona estado = mapaCorporalService.estadoDe(evaluacion, microzonaId);
        Recomendacion recomendacion = mapaCorporalService.recomendar(evaluacion);
        String advertencia = construirAdvertencia(estado);
        return new ValidacionAplicacionResponse(estado.getMicrozona().id(), estado.getSemaforo(), true,
                advertencia != null, advertencia, afectadas(evaluacion.mapa(), estado.getMicrozona().id()),
                MicrozonaEstadoResponse.desde(estado),
                MicrozonaEstadoResponse.desde(recomendacion.principal().estado()));
    }

    /** Registra una inyeccion (RF03). Bloquea la microzona y sus adyacentes (RF07) y la apila para deshacer. */
    public RegistroAplicacionResponse registrar(String pacienteId, RegistrarAplicacionRequest solicitud) {
        return registrar(pacienteId, solicitud, null);
    }

    public RegistroAplicacionResponse registrar(String pacienteId, RegistrarAplicacionRequest solicitud,
            String recordatorioId) {
        Paciente paciente = pacienteService.obtener(pacienteId);
        Instant ahora = Instant.now(reloj);
        Instant fechaHora = solicitud.fechaHora() == null ? ahora : solicitud.fechaHora();
        if (fechaHora.isAfter(ahora.plus(TOLERANCIA_FUTURO))) {
            throw new SolicitudInvalidaException("La fecha de la aplicacion no puede estar en el futuro");
        }

        ResultadoEvaluacion evaluacion = mapaCorporalService.evaluar(paciente);
        EstadoMicrozona estado = mapaCorporalService.estadoDe(evaluacion, solicitud.microzonaId());
        Recomendacion recomendacion = mapaCorporalService.recomendar(evaluacion);
        Microzona microzona = estado.getMicrozona();

        Aplicacion aplicacion = new Aplicacion();
        aplicacion.setPacienteId(paciente.getId());
        aplicacion.setMicrozonaId(microzona.id());
        aplicacion.setZonaMacro(microzona.zonaMacro());
        aplicacion.setLado(microzona.lado());
        aplicacion.setFila(microzona.fila());
        aplicacion.setColumna(microzona.columna());
        aplicacion.setTipoInsulina(solicitud.tipoInsulina());
        aplicacion.setUnidades(solicitud.unidades());
        aplicacion.setFechaHora(fechaHora);
        aplicacion.setRegistradoPor(solicitud.registradoPor() == null ? RegistradoPor.PACIENTE : solicitud.registradoPor());
        aplicacion.setNotas(solicitud.notas());
        aplicacion.setSemaforoAlRegistrar(estado.getSemaforo());
        aplicacion.setEnZonaRecomendada(microzona.id().equals(recomendacion.principal().estado().getMicrozona().id()));
        aplicacion.setRecordatorioId(recordatorioId);
        Aplicacion guardada = aplicacionRepository.save(aplicacion);

        PilaDeshacer pila = PilaDeshacer.desdeLista(paciente.getPilaDeshacer());
        pila.apilar(guardada.getId());
        paciente.setPilaDeshacer(pila.aLista());
        pacienteService.guardar(paciente);

        return new RegistroAplicacionResponse(AplicacionResponse.desde(guardada), construirAdvertencia(estado),
                afectadas(evaluacion.mapa(), microzona.id()), pila.tamano());
    }

    /** Deshace el ultimo registro (RF05): desapila y restablece el estado previo de las microzonas. */
    public DeshacerResponse deshacerUltima(String pacienteId) {
        Paciente paciente = pacienteService.obtener(pacienteId);
        PilaDeshacer pila = PilaDeshacer.desdeLista(paciente.getPilaDeshacer());
        Aplicacion aDeshacer = null;
        while (aDeshacer == null) {
            Optional<String> cima = pila.desapilar();
            if (cima.isEmpty()) {
                paciente.setPilaDeshacer(pila.aLista());
                pacienteService.guardar(paciente);
                throw new ReglaNegocioException("No hay registros de inyeccion para deshacer");
            }
            aDeshacer = aplicacionRepository.findById(cima.get()).filter(a -> !a.isDeshecha()).orElse(null);
        }
        aDeshacer.setDeshecha(true);
        aDeshacer.setFechaDeshecha(Instant.now(reloj));
        Aplicacion guardada = aplicacionRepository.save(aDeshacer);
        paciente.setPilaDeshacer(pila.aLista());
        pacienteService.guardar(paciente);

        MapaCorporal mapa = new MapaCorporal(paciente.getConfiguracionMapa());
        List<String> restablecidas = mapa.existe(guardada.getMicrozonaId())
                ? afectadas(mapa, guardada.getMicrozonaId()) : List.of(guardada.getMicrozonaId());
        return new DeshacerResponse(AplicacionResponse.desde(guardada), restablecidas, pila.tamano());
    }

    /** Historial completo de aplicaciones activas, opcionalmente filtrado (RF11). */
    public List<AplicacionResponse> historial(String pacienteId, Instant desde, Instant hasta, ZonaMacro zona) {
        return consultarHistorial(pacienteId, desde, hasta, zona).stream().map(AplicacionResponse::desde).toList();
    }

    public List<Aplicacion> consultarHistorial(String pacienteId, Instant desde, Instant hasta, ZonaMacro zona) {
        pacienteService.obtener(pacienteId);
        Instant inicio = desde == null ? Instant.EPOCH : desde;
        Instant fin = hasta == null ? Instant.now(reloj).plus(TOLERANCIA_FUTURO) : hasta;
        if (inicio.isAfter(fin)) {
            throw new SolicitudInvalidaException("La fecha 'desde' debe ser anterior a 'hasta'");
        }
        return aplicacionRepository
                .findByPacienteIdAndDeshechaFalseAndFechaHoraBetweenOrderByFechaHoraDesc(pacienteId, inicio, fin)
                .stream()
                .filter(a -> zona == null || a.getZonaMacro() == zona)
                .toList();
    }

    public AplicacionResponse consultar(String pacienteId, String aplicacionId) {
        return aplicacionRepository.findById(aplicacionId)
                .filter(a -> a.getPacienteId().equals(pacienteId))
                .map(AplicacionResponse::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException("Aplicacion", aplicacionId));
    }

    private List<String> afectadas(MapaCorporal mapa, String microzonaId) {
        List<String> ids = new ArrayList<>();
        ids.add(microzonaId);
        ids.addAll(mapa.vecinos(microzonaId));
        return ids;
    }

    private String construirAdvertencia(EstadoMicrozona estado) {
        if (estado.getSemaforo() == Semaforo.VERDE) {
            return null;
        }
        StringBuilder mensaje = new StringBuilder();
        mensaje.append(estado.getSemaforo() == Semaforo.ROJO
                ? "Microzona bloqueada: aplicacion reciente"
                : "Microzona en recuperacion");
        if (estado.isBloqueadaPorAdyacencia()) {
            mensaje.append(" (adyacente a ").append(estado.getOrigenBloqueo()).append(')');
        }
        mensaje.append(". Recuperacion: ").append(Math.round(estado.getProgresoRecuperacion() * 100)).append('%');
        if (estado.getDisponibleDesde() != null) {
            mensaje.append(", disponible desde ").append(FORMATO_HORA.format(estado.getDisponibleDesde()));
        }
        mensaje.append(". Inyectar en tejido no recuperado puede causar lipodistrofia y absorcion erratica. "
                + "El registro se permite si usted lo decide.");
        return mensaje.toString();
    }
}
