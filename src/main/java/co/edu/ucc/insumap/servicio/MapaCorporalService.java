package co.edu.ucc.insumap.servicio;

import co.edu.ucc.insumap.config.ParametrosMotor;
import co.edu.ucc.insumap.dto.mapa.CuadriculaResponse;
import co.edu.ucc.insumap.dto.mapa.EstadisticasResponse;
import co.edu.ucc.insumap.dto.mapa.MapaCorporalResponse;
import co.edu.ucc.insumap.dto.mapa.MicrozonaDetalleResponse;
import co.edu.ucc.insumap.dto.mapa.MicrozonaEstadoResponse;
import co.edu.ucc.insumap.dto.mapa.RecomendacionResponse;
import co.edu.ucc.insumap.dto.mapa.ResumenSemaforoResponse;
import co.edu.ucc.insumap.dto.mapa.ZonaMacroResponse;
import co.edu.ucc.insumap.excepcion.RecursoNoEncontradoException;
import co.edu.ucc.insumap.modelo.Aplicacion;
import co.edu.ucc.insumap.modelo.Paciente;
import co.edu.ucc.insumap.modelo.enums.Lado;
import co.edu.ucc.insumap.modelo.enums.Semaforo;
import co.edu.ucc.insumap.modelo.enums.ZonaMacro;
import co.edu.ucc.insumap.motor.EstadoMicrozona;
import co.edu.ucc.insumap.motor.MapaCorporal;
import co.edu.ucc.insumap.motor.MotorRecomendacion;
import co.edu.ucc.insumap.motor.Recomendacion;
import co.edu.ucc.insumap.motor.ResultadoEvaluacion;
import co.edu.ucc.insumap.motor.ResumenZonaMacro;
import co.edu.ucc.insumap.motor.estructuras.CuadriculaZona;
import co.edu.ucc.insumap.motor.estructuras.Microzona;
import co.edu.ucc.insumap.repositorio.AplicacionRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/** Construye el mapa corporal del paciente con su semaforo y la zona sugerida (RF01, RF02, RF06 - RF09). */
@Service
public class MapaCorporalService {

    private final PacienteService pacienteService;
    private final AplicacionRepository aplicacionRepository;
    private final MotorRecomendacion motor;
    private final ParametrosMotor parametrosMotor;
    private final Clock reloj;

    public MapaCorporalService(PacienteService pacienteService, AplicacionRepository aplicacionRepository,
            MotorRecomendacion motor, ParametrosMotor parametrosMotor, Clock reloj) {
        this.pacienteService = pacienteService;
        this.aplicacionRepository = aplicacionRepository;
        this.motor = motor;
        this.parametrosMotor = parametrosMotor;
        this.reloj = reloj;
    }

    /** Evalua el estado actual de todas las microzonas del paciente. */
    public ResultadoEvaluacion evaluar(Paciente paciente) {
        Instant ahora = Instant.now(reloj);
        Instant desde = ahora.minus(Duration.ofDays(motor.diasHistorialRequeridos()));
        List<Aplicacion> historial = aplicacionRepository
                .findByPacienteIdAndDeshechaFalseAndFechaHoraAfterOrderByFechaHoraDesc(paciente.getId(), desde);
        return motor.evaluar(new MapaCorporal(paciente.getConfiguracionMapa()), historial, ahora);
    }

    public Recomendacion recomendar(ResultadoEvaluacion evaluacion) {
        return motor.recomendar(evaluacion);
    }

    public MapaCorporalResponse obtenerMapa(String pacienteId) {
        Paciente paciente = pacienteService.obtener(pacienteId);
        ResultadoEvaluacion evaluacion = evaluar(paciente);
        MapaCorporal mapa = evaluacion.mapa();
        List<ZonaMacroResponse> zonas = new ArrayList<>();
        for (ZonaMacro zona : ZonaMacro.values()) {
            List<CuadriculaResponse> lados = new ArrayList<>();
            for (Lado lado : Lado.values()) {
                lados.add(aCuadricula(mapa.cuadricula(zona, lado), evaluacion));
            }
            ResumenZonaMacro r = evaluacion.resumenZonas().get(zona);
            zonas.add(new ZonaMacroResponse(zona, zona.getNombre(), r.usosEnVentana(),
                    MicrozonaEstadoResponse.redondear(r.diasSinUso()), r.saturada(), r.olvidada(), r.factorPrioridad(),
                    lados));
        }
        return new MapaCorporalResponse(paciente.getId(), mapa.getFilas(), mapa.getColumnas(),
                evaluacion.evaluadoEn(), ResumenSemaforoResponse.desde(evaluacion.estados()),
                RecomendacionResponse.desde(motor.recomendar(evaluacion)), zonas);
    }

    public MicrozonaDetalleResponse obtenerMicrozona(String pacienteId, String microzonaId) {
        Paciente paciente = pacienteService.obtener(pacienteId);
        ResultadoEvaluacion evaluacion = evaluar(paciente);
        EstadoMicrozona estado = estadoDe(evaluacion, microzonaId);
        List<MicrozonaEstadoResponse> vecinas = evaluacion.mapa().vecinos(estado.getMicrozona().id()).stream()
                .map(id -> MicrozonaEstadoResponse.desde(evaluacion.estados().obtener(id).orElseThrow()))
                .toList();
        return new MicrozonaDetalleResponse(MicrozonaEstadoResponse.desde(estado), vecinas);
    }

    public RecomendacionResponse obtenerRecomendacion(String pacienteId) {
        Paciente paciente = pacienteService.obtener(pacienteId);
        return RecomendacionResponse.desde(motor.recomendar(evaluar(paciente)));
    }

    public EstadisticasResponse obtenerEstadisticas(String pacienteId) {
        Paciente paciente = pacienteService.obtener(pacienteId);
        Instant ahora = Instant.now(reloj);
        Instant inicioVentana = ahora.minus(Duration.ofDays(parametrosMotor.diasVentana()));
        List<Aplicacion> enVentana = aplicacionRepository
                .findByPacienteIdAndDeshechaFalseAndFechaHoraBetweenOrderByFechaHoraDesc(pacienteId, inicioVentana, ahora);
        ResultadoEvaluacion evaluacion = evaluar(paciente);

        int total = enVentana.size();
        long adherentes = enVentana.stream().filter(Aplicacion::isEnZonaRecomendada).count();
        long conAdvertencia = enVentana.stream()
                .filter(a -> a.getSemaforoAlRegistrar() != null && a.getSemaforoAlRegistrar() != Semaforo.VERDE)
                .count();
        List<EstadisticasResponse.UsoZonaResponse> zonas = new ArrayList<>();
        for (ZonaMacro zona : ZonaMacro.values()) {
            ResumenZonaMacro r = evaluacion.resumenZonas().get(zona);
            double porcentaje = total == 0 ? 0 : r.usosEnVentana() * 100.0 / total;
            zonas.add(new EstadisticasResponse.UsoZonaResponse(zona, r.usosEnVentana(),
                    MicrozonaEstadoResponse.redondear(porcentaje), MicrozonaEstadoResponse.redondear(r.diasSinUso()),
                    r.saturada(), r.olvidada()));
        }
        return new EstadisticasResponse(pacienteId, parametrosMotor.diasVentana(), total,
                MicrozonaEstadoResponse.redondear(evaluacion.promedioUsosPorZona()),
                total == 0 ? 0 : MicrozonaEstadoResponse.redondear(adherentes * 100.0 / total), (int) conAdvertencia,
                ResumenSemaforoResponse.desde(evaluacion.estados()), zonas);
    }

    /** Estado de una microzona o 404 si no pertenece a la cuadricula del paciente. */
    public EstadoMicrozona estadoDe(ResultadoEvaluacion evaluacion, String microzonaId) {
        Microzona microzona = evaluacion.mapa().buscar(microzonaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Microzona", microzonaId));
        return evaluacion.estados().obtener(microzona.id()).orElseThrow();
    }

    private CuadriculaResponse aCuadricula(CuadriculaZona cuadricula, ResultadoEvaluacion evaluacion) {
        List<List<MicrozonaEstadoResponse>> filas = new ArrayList<>();
        for (int f = 0; f < cuadricula.getFilas(); f++) {
            List<MicrozonaEstadoResponse> fila = new ArrayList<>();
            for (int c = 0; c < cuadricula.getColumnas(); c++) {
                String id = cuadricula.obtener(f, c).id();
                fila.add(MicrozonaEstadoResponse.desde(evaluacion.estados().obtener(id).orElseThrow()));
            }
            filas.add(fila);
        }
        return new CuadriculaResponse(cuadricula.getLado(), cuadricula.getFilas(), cuadricula.getColumnas(), filas);
    }
}
