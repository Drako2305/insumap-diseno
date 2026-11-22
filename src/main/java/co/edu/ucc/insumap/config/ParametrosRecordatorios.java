package co.edu.ucc.insumap.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuracion del planificador de recordatorios (prefijo "insumap.recordatorios", RF10). */
@ConfigurationProperties(prefix = "insumap.recordatorios")
public record ParametrosRecordatorios(
        boolean habilitado,
        long intervaloMs,
        String zonaHoraria,
        int minutosAplazamiento,
        int maximoAplazamientos) {
}
