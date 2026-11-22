package co.edu.ucc.insumap.servicio;

import co.edu.ucc.insumap.dto.recordatorio.CicloRecordatoriosResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Ejecuta periodicamente el ciclo de recordatorios (RF10). Se desactiva con insumap.recordatorios.habilitado=false. */
@Component
@ConditionalOnProperty(prefix = "insumap.recordatorios", name = "habilitado", havingValue = "true")
public class PlanificadorRecordatorios {

    private static final Logger LOG = LoggerFactory.getLogger(PlanificadorRecordatorios.class);

    private final RecordatorioService recordatorioService;

    public PlanificadorRecordatorios(RecordatorioService recordatorioService) {
        this.recordatorioService = recordatorioService;
    }

    @Scheduled(fixedDelayString = "${insumap.recordatorios.intervalo-ms}",
            initialDelayString = "${insumap.recordatorios.intervalo-ms}")
    public void ejecutar() {
        try {
            CicloRecordatoriosResponse ciclo = recordatorioService.ejecutarCiclo();
            if (ciclo.generados() > 0 || !ciclo.despachados().isEmpty()) {
                LOG.info("Ciclo de recordatorios: {} generados, {} despachados", ciclo.generados(),
                        ciclo.despachados().size());
            }
        } catch (RuntimeException e) {
            LOG.warn("Fallo el ciclo de recordatorios: {}", e.getMessage());
        }
    }
}
