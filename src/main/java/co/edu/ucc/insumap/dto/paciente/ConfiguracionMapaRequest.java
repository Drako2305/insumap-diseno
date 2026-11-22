package co.edu.ucc.insumap.dto.paciente;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** Tamano de la cuadricula de microzonas de cada zona macro y lado (RF02). */
public record ConfiguracionMapaRequest(
        @Min(1) @Max(8) int filas,
        @Min(1) @Max(8) int columnas) {
}
