package co.edu.ucc.insumap.servicio;

import co.edu.ucc.insumap.config.ParametrosMapa;
import co.edu.ucc.insumap.dto.paciente.ConfiguracionMapaRequest;
import co.edu.ucc.insumap.dto.paciente.CuidadorDto;
import co.edu.ucc.insumap.dto.paciente.PacienteRequest;
import co.edu.ucc.insumap.dto.paciente.PacienteResponse;
import co.edu.ucc.insumap.excepcion.RecursoNoEncontradoException;
import co.edu.ucc.insumap.excepcion.ReglaNegocioException;
import co.edu.ucc.insumap.excepcion.SolicitudInvalidaException;
import co.edu.ucc.insumap.modelo.ConfiguracionMapa;
import co.edu.ucc.insumap.modelo.Paciente;
import co.edu.ucc.insumap.repositorio.AplicacionRepository;
import co.edu.ucc.insumap.repositorio.CronogramaRepository;
import co.edu.ucc.insumap.repositorio.PacienteRepository;
import co.edu.ucc.insumap.repositorio.RecordatorioRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/** Gestion de pacientes, sus cuidadores, su medico y la configuracion de su mapa (RF02). */
@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final AplicacionRepository aplicacionRepository;
    private final CronogramaRepository cronogramaRepository;
    private final RecordatorioRepository recordatorioRepository;
    private final ParametrosMapa parametrosMapa;
    private final Clock reloj;

    public PacienteService(PacienteRepository pacienteRepository, AplicacionRepository aplicacionRepository,
            CronogramaRepository cronogramaRepository, RecordatorioRepository recordatorioRepository,
            ParametrosMapa parametrosMapa, Clock reloj) {
        this.pacienteRepository = pacienteRepository;
        this.aplicacionRepository = aplicacionRepository;
        this.cronogramaRepository = cronogramaRepository;
        this.recordatorioRepository = recordatorioRepository;
        this.parametrosMapa = parametrosMapa;
        this.reloj = reloj;
    }

    public PacienteResponse crear(PacienteRequest solicitud) {
        if (pacienteRepository.existsByDocumentoIdentidad(solicitud.documentoIdentidad().trim())) {
            throw new ReglaNegocioException("Ya existe un paciente con el documento " + solicitud.documentoIdentidad());
        }
        Paciente paciente = new Paciente();
        aplicarDatos(paciente, solicitud);
        paciente.setConfiguracionMapa(solicitud.configuracionMapa() == null
                ? new ConfiguracionMapa(parametrosMapa.filasPorDefecto(), parametrosMapa.columnasPorDefecto())
                : construirConfiguracion(solicitud.configuracionMapa()));
        paciente.setFechaRegistro(Instant.now(reloj));
        return aRespuesta(pacienteRepository.save(paciente));
    }

    public List<PacienteResponse> listar() {
        return pacienteRepository.findAll(Sort.by("apellidos", "nombres")).stream().map(this::aRespuesta).toList();
    }

    public PacienteResponse consultar(String id) {
        return aRespuesta(obtener(id));
    }

    /** Devuelve la entidad del paciente o lanza 404. */
    public Paciente obtener(String id) {
        return pacienteRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Paciente", id));
    }

    public PacienteResponse actualizar(String id, PacienteRequest solicitud) {
        Paciente paciente = obtener(id);
        String documento = solicitud.documentoIdentidad().trim();
        if (!documento.equals(paciente.getDocumentoIdentidad()) && pacienteRepository.existsByDocumentoIdentidad(documento)) {
            throw new ReglaNegocioException("Ya existe un paciente con el documento " + documento);
        }
        aplicarDatos(paciente, solicitud);
        if (solicitud.configuracionMapa() != null) {
            paciente.setConfiguracionMapa(construirConfiguracion(solicitud.configuracionMapa()));
        }
        return aRespuesta(pacienteRepository.save(paciente));
    }

    /** Cambia el tamano de la cuadricula de microzonas (RF02). */
    public PacienteResponse actualizarConfiguracion(String id, ConfiguracionMapaRequest solicitud) {
        Paciente paciente = obtener(id);
        paciente.setConfiguracionMapa(construirConfiguracion(solicitud));
        return aRespuesta(pacienteRepository.save(paciente));
    }

    /** Elimina el paciente y todos sus datos (control exclusivo del usuario sobre sus datos de salud). */
    public void eliminar(String id) {
        Paciente paciente = obtener(id);
        aplicacionRepository.deleteByPacienteId(paciente.getId());
        recordatorioRepository.deleteByPacienteId(paciente.getId());
        cronogramaRepository.deleteByPacienteId(paciente.getId());
        pacienteRepository.delete(paciente);
    }

    public Paciente guardar(Paciente paciente) {
        return pacienteRepository.save(paciente);
    }

    private void aplicarDatos(Paciente paciente, PacienteRequest solicitud) {
        paciente.setNombres(solicitud.nombres().trim());
        paciente.setApellidos(solicitud.apellidos().trim());
        paciente.setDocumentoIdentidad(solicitud.documentoIdentidad().trim());
        paciente.setFechaNacimiento(solicitud.fechaNacimiento());
        paciente.setTipoDiabetes(solicitud.tipoDiabetes());
        paciente.setEmail(solicitud.email());
        paciente.setTelefono(solicitud.telefono());
        paciente.setCuidadores(solicitud.cuidadores() == null ? null
                : solicitud.cuidadores().stream().map(CuidadorDto::aEntidad).toList());
        paciente.setMedicoTratante(solicitud.medicoTratante() == null ? null : solicitud.medicoTratante().aEntidad());
    }

    private ConfiguracionMapa construirConfiguracion(ConfiguracionMapaRequest solicitud) {
        int maximo = parametrosMapa.tamanoMaximo();
        if (solicitud.filas() > maximo || solicitud.columnas() > maximo) {
            throw new SolicitudInvalidaException("La cuadricula admite como maximo " + maximo + " filas y columnas");
        }
        return new ConfiguracionMapa(solicitud.filas(), solicitud.columnas());
    }

    private PacienteResponse aRespuesta(Paciente paciente) {
        return PacienteResponse.desde(paciente, LocalDate.now(reloj));
    }
}
