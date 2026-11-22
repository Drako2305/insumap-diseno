package co.edu.ucc.insumap.dto.aplicacion;

import co.edu.ucc.insumap.modelo.enums.RegistradoPor;
import co.edu.ucc.insumap.modelo.enums.TipoInsulina;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * Registro de una inyeccion tocando una microzona del mapa (RF03).
 *
 * @param fechaHora opcional; si se omite se usa la hora actual (registro en un solo toque)
 */
public record RegistrarAplicacionRequest(
        @NotBlank @Size(max = 20) String microzonaId,
        @NotNull TipoInsulina tipoInsulina,
        @DecimalMin(value = "0.5") @DecimalMax(value = "300") double unidades,
        Instant fechaHora,
        RegistradoPor registradoPor,
        @Size(max = 500) String notas) {
}
