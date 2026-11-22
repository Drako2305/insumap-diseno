package co.edu.ucc.insumap.controlador;

import co.edu.ucc.insumap.dto.aplicacion.AplicacionResponse;
import co.edu.ucc.insumap.dto.aplicacion.DeshacerResponse;
import co.edu.ucc.insumap.dto.aplicacion.RegistrarAplicacionRequest;
import co.edu.ucc.insumap.dto.aplicacion.RegistroAplicacionResponse;
import co.edu.ucc.insumap.dto.aplicacion.ValidacionAplicacionResponse;
import co.edu.ucc.insumap.modelo.enums.ZonaMacro;
import co.edu.ucc.insumap.servicio.AplicacionService;
import co.edu.ucc.insumap.servicio.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pacientes/{pacienteId}/aplicaciones")
@Tag(name = "3. Aplicaciones", description = "Registro de inyecciones, advertencias, deshacer e historial (RF03 - RF05, RF07, RF11)")
public class AplicacionController {

    private final AplicacionService aplicacionService;
    private final ReporteService reporteService;

    public AplicacionController(AplicacionService aplicacionService, ReporteService reporteService) {
        this.aplicacionService = aplicacionService;
        this.reporteService = reporteService;
    }

    @GetMapping("/validar/{microzonaId}")
    @Operation(summary = "Validar una microzona antes de registrar: devuelve la advertencia sin bloquear (RF04)")
    public ValidacionAplicacionResponse validar(@PathVariable String pacienteId, @PathVariable String microzonaId) {
        return aplicacionService.validar(pacienteId, microzonaId);
    }

    @PostMapping
    @Operation(summary = "Registrar una inyeccion tocando la microzona (RF03, RF07)")
    public ResponseEntity<RegistroAplicacionResponse> registrar(@PathVariable String pacienteId,
            @Valid @RequestBody RegistrarAplicacionRequest solicitud) {
        RegistroAplicacionResponse registro = aplicacionService.registrar(pacienteId, solicitud);
        return ResponseEntity
                .created(URI.create("/api/pacientes/" + pacienteId + "/aplicaciones/" + registro.aplicacion().id()))
                .body(registro);
    }

    @PostMapping("/deshacer")
    @Operation(summary = "Deshacer el ultimo registro de inyeccion (RF05)")
    public DeshacerResponse deshacer(@PathVariable String pacienteId) {
        return aplicacionService.deshacerUltima(pacienteId);
    }

    @GetMapping
    @Operation(summary = "Historial de aplicaciones, filtrable por fechas (ISO-8601) y zona macro (RF11)")
    public List<AplicacionResponse> historial(@PathVariable String pacienteId,
            @RequestParam(required = false) Instant desde, @RequestParam(required = false) Instant hasta,
            @RequestParam(required = false) ZonaMacro zona) {
        return aplicacionService.historial(pacienteId, desde, hasta, zona);
    }

    @GetMapping("/{aplicacionId}")
    @Operation(summary = "Consultar una aplicacion")
    public AplicacionResponse consultar(@PathVariable String pacienteId, @PathVariable String aplicacionId) {
        return aplicacionService.consultar(pacienteId, aplicacionId);
    }

    @GetMapping(value = "/exportar/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Exportar el historial en PDF para el medico tratante (RF11)")
    public ResponseEntity<byte[]> exportarPdf(@PathVariable String pacienteId,
            @RequestParam(required = false) Instant desde, @RequestParam(required = false) Instant hasta) {
        byte[] pdf = reporteService.generarHistorialPdf(pacienteId, desde, hasta);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("insumap-historial-" + pacienteId + ".pdf").build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
