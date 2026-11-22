package co.edu.ucc.insumap.controlador;

import co.edu.ucc.insumap.dto.mapa.EstadisticasResponse;
import co.edu.ucc.insumap.dto.mapa.MapaCorporalResponse;
import co.edu.ucc.insumap.dto.mapa.MicrozonaDetalleResponse;
import co.edu.ucc.insumap.dto.mapa.RecomendacionResponse;
import co.edu.ucc.insumap.servicio.MapaCorporalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pacientes/{pacienteId}")
@Tag(name = "2. Mapa corporal", description = "Mapa 2D, semaforo, zona sugerida y estadisticas (RF01, RF06 - RF09)")
public class MapaCorporalController {

    private final MapaCorporalService mapaCorporalService;

    public MapaCorporalController(MapaCorporalService mapaCorporalService) {
        this.mapaCorporalService = mapaCorporalService;
    }

    @GetMapping("/mapa")
    @Operation(summary = "Mapa corporal completo con el semaforo de cada microzona y la zona sugerida")
    public MapaCorporalResponse mapa(@PathVariable String pacienteId) {
        return mapaCorporalService.obtenerMapa(pacienteId);
    }

    @GetMapping("/mapa/microzonas/{microzonaId}")
    @Operation(summary = "Estado de una microzona y de sus vecinas en el grafo de adyacencia")
    public MicrozonaDetalleResponse microzona(@PathVariable String pacienteId, @PathVariable String microzonaId) {
        return mapaCorporalService.obtenerMicrozona(pacienteId, microzonaId);
    }

    @GetMapping("/recomendacion")
    @Operation(summary = "Zona optima para la siguiente dosis (RF08, RF09)")
    public RecomendacionResponse recomendacion(@PathVariable String pacienteId) {
        return mapaCorporalService.obtenerRecomendacion(pacienteId);
    }

    @GetMapping("/estadisticas")
    @Operation(summary = "Uso por zona macro, zonas saturadas/olvidadas y adherencia a la rotacion")
    public EstadisticasResponse estadisticas(@PathVariable String pacienteId) {
        return mapaCorporalService.obtenerEstadisticas(pacienteId);
    }
}
