package co.edu.ucc.insumap.controlador;

import co.edu.ucc.insumap.dto.recordatorio.AplazarRecordatorioRequest;
import co.edu.ucc.insumap.dto.recordatorio.CicloRecordatoriosResponse;
import co.edu.ucc.insumap.dto.recordatorio.ConfirmacionRecordatorioResponse;
import co.edu.ucc.insumap.dto.recordatorio.ConfirmarRecordatorioRequest;
import co.edu.ucc.insumap.dto.recordatorio.RecordatorioResponse;
import co.edu.ucc.insumap.modelo.enums.EstadoRecordatorio;
import co.edu.ucc.insumap.servicio.RecordatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "5. Recordatorios", description = "Notificaciones de dosis: confirmar, aplazar u omitir (RF10)")
public class RecordatorioController {

    private final RecordatorioService recordatorioService;

    public RecordatorioController(RecordatorioService recordatorioService) {
        this.recordatorioService = recordatorioService;
    }

    @GetMapping("/pacientes/{pacienteId}/recordatorios")
    @Operation(summary = "Listar recordatorios del paciente (filtro opcional por estado)")
    public List<RecordatorioResponse> listar(@PathVariable String pacienteId,
            @RequestParam(required = false) EstadoRecordatorio estado) {
        return recordatorioService.listar(pacienteId, estado);
    }

    @GetMapping("/recordatorios/{recordatorioId}")
    @Operation(summary = "Consultar recordatorio")
    public RecordatorioResponse consultar(@PathVariable String recordatorioId) {
        return recordatorioService.consultar(recordatorioId);
    }

    @PostMapping("/recordatorios/{recordatorioId}/confirmar")
    @Operation(summary = "Confirmar la dosis; con microzonaId registra tambien la inyeccion")
    public ConfirmacionRecordatorioResponse confirmar(@PathVariable String recordatorioId,
            @Valid @RequestBody(required = false) ConfirmarRecordatorioRequest solicitud) {
        return recordatorioService.confirmar(recordatorioId, solicitud);
    }

    @PostMapping("/recordatorios/{recordatorioId}/aplazar")
    @Operation(summary = "Aplazar el recordatorio unos minutos")
    public RecordatorioResponse aplazar(@PathVariable String recordatorioId,
            @Valid @RequestBody(required = false) AplazarRecordatorioRequest solicitud) {
        return recordatorioService.aplazar(recordatorioId, solicitud == null ? null : solicitud.minutos());
    }

    @PostMapping("/recordatorios/{recordatorioId}/omitir")
    @Operation(summary = "Omitir la dosis recordada")
    public RecordatorioResponse omitir(@PathVariable String recordatorioId) {
        return recordatorioService.omitir(recordatorioId);
    }

    @PostMapping("/recordatorios/procesar")
    @Operation(summary = "Ejecutar manualmente el ciclo del planificador (generar y despachar)")
    public CicloRecordatoriosResponse procesar() {
        return recordatorioService.ejecutarCiclo();
    }
}
