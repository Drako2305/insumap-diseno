package co.edu.ucc.insumap.controlador;

import co.edu.ucc.insumap.dto.cronograma.CronogramaRequest;
import co.edu.ucc.insumap.dto.cronograma.CronogramaResponse;
import co.edu.ucc.insumap.servicio.CronogramaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pacientes/{pacienteId}/cronogramas")
@Tag(name = "4. Cronogramas", description = "Cronograma de dosis del paciente (RF10)")
public class CronogramaController {

    private final CronogramaService cronogramaService;

    public CronogramaController(CronogramaService cronogramaService) {
        this.cronogramaService = cronogramaService;
    }

    @PostMapping
    @Operation(summary = "Crear cronograma de dosis")
    public ResponseEntity<CronogramaResponse> crear(@PathVariable String pacienteId,
            @Valid @RequestBody CronogramaRequest solicitud) {
        CronogramaResponse creado = cronogramaService.crear(pacienteId, solicitud);
        return ResponseEntity.created(URI.create("/api/pacientes/" + pacienteId + "/cronogramas/" + creado.id()))
                .body(creado);
    }

    @GetMapping
    @Operation(summary = "Listar cronogramas del paciente")
    public List<CronogramaResponse> listar(@PathVariable String pacienteId) {
        return cronogramaService.listar(pacienteId);
    }

    @GetMapping("/{cronogramaId}")
    @Operation(summary = "Consultar cronograma")
    public CronogramaResponse consultar(@PathVariable String pacienteId, @PathVariable String cronogramaId) {
        return cronogramaService.consultar(pacienteId, cronogramaId);
    }

    @PutMapping("/{cronogramaId}")
    @Operation(summary = "Actualizar cronograma")
    public CronogramaResponse actualizar(@PathVariable String pacienteId, @PathVariable String cronogramaId,
            @Valid @RequestBody CronogramaRequest solicitud) {
        return cronogramaService.actualizar(pacienteId, cronogramaId, solicitud);
    }

    @PatchMapping("/{cronogramaId}/estado")
    @Operation(summary = "Activar o desactivar un cronograma")
    public CronogramaResponse cambiarEstado(@PathVariable String pacienteId, @PathVariable String cronogramaId,
            @RequestParam boolean activo) {
        return cronogramaService.cambiarEstado(pacienteId, cronogramaId, activo);
    }

    @DeleteMapping("/{cronogramaId}")
    @Operation(summary = "Eliminar cronograma")
    public ResponseEntity<Void> eliminar(@PathVariable String pacienteId, @PathVariable String cronogramaId) {
        cronogramaService.eliminar(pacienteId, cronogramaId);
        return ResponseEntity.noContent().build();
    }
}
