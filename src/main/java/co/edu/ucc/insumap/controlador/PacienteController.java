package co.edu.ucc.insumap.controlador;

import co.edu.ucc.insumap.dto.paciente.ConfiguracionMapaRequest;
import co.edu.ucc.insumap.dto.paciente.PacienteRequest;
import co.edu.ucc.insumap.dto.paciente.PacienteResponse;
import co.edu.ucc.insumap.servicio.PacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pacientes")
@Tag(name = "1. Pacientes", description = "Gestion de pacientes, cuidadores, medico tratante y cuadricula (RF02)")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @PostMapping
    @Operation(summary = "Registrar paciente")
    public ResponseEntity<PacienteResponse> crear(@Valid @RequestBody PacienteRequest solicitud) {
        PacienteResponse creado = pacienteService.crear(solicitud);
        return ResponseEntity.created(URI.create("/api/pacientes/" + creado.id())).body(creado);
    }

    @GetMapping
    @Operation(summary = "Listar pacientes")
    public List<PacienteResponse> listar() {
        return pacienteService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar paciente")
    public PacienteResponse consultar(@PathVariable String id) {
        return pacienteService.consultar(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar paciente")
    public PacienteResponse actualizar(@PathVariable String id, @Valid @RequestBody PacienteRequest solicitud) {
        return pacienteService.actualizar(id, solicitud);
    }

    @PutMapping("/{id}/configuracion-mapa")
    @Operation(summary = "Configurar el tamano de la cuadricula de microzonas (RF02)")
    public PacienteResponse configurarMapa(@PathVariable String id,
            @Valid @RequestBody ConfiguracionMapaRequest solicitud) {
        return pacienteService.actualizarConfiguracion(id, solicitud);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar paciente y todos sus datos")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        pacienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
