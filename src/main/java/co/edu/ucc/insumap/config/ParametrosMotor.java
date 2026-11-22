package co.edu.ucc.insumap.config;

import co.edu.ucc.insumap.modelo.enums.ZonaMacro;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Parametros configurables del motor de recomendacion (prefijo "insumap.motor").
 * Ver application.properties para el significado de cada valor.
 */
@ConfigurationProperties(prefix = "insumap.motor")
public record ParametrosMotor(
        double horasBaseAbdomen,
        double horasBaseMuslos,
        double horasBaseBrazos,
        double horasBaseGluteos,
        double factorFrecuencia,
        double factorFrecuenciaMaximo,
        double factorAdyacencia,
        double umbralRojo,
        int diasVentana,
        int diasZonaOlvidada,
        double penalizacionZonaSaturada,
        double bonificacionZonaOlvidada,
        int alternativas) {

    /** Valores por defecto (los mismos de application.properties), utiles en pruebas. */
    public static ParametrosMotor porDefecto() {
        return new ParametrosMotor(72, 96, 96, 120, 0.25, 2.0, 0.5, 0.5, 30, 15, 0.6, 1.5, 3);
    }

    /** Horas base de recuperacion biologica de una zona macro (RF08). */
    public double horasBase(ZonaMacro zona) {
        return switch (zona) {
            case ABDOMEN -> horasBaseAbdomen;
            case MUSLOS -> horasBaseMuslos;
            case BRAZOS -> horasBaseBrazos;
            case GLUTEOS -> horasBaseGluteos;
        };
    }
}
