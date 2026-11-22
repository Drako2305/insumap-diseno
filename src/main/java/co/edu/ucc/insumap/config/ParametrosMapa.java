package co.edu.ucc.insumap.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Tamano por defecto y maximo de la cuadricula de microzonas (prefijo "insumap.mapa", RF02). */
@ConfigurationProperties(prefix = "insumap.mapa")
public record ParametrosMapa(int filasPorDefecto, int columnasPorDefecto, int tamanoMaximo) {
}
