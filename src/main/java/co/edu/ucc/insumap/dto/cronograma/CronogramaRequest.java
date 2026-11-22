package co.edu.ucc.insumap.dto.cronograma;

import co.edu.ucc.insumap.modelo.enums.TipoInsulina;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;

/**
 * Cronograma de dosis (RF10).
 *
 * @param horas      horas del dia en formato HH:mm, por ejemplo ["07:00", "19:00"]
 * @param diasSemana dias en que aplica (MONDAY..SUNDAY); si se omite aplica todos los dias
 */
public record CronogramaRequest(
        @NotBlank @Size(max = 80) String nombre,
        @NotNull TipoInsulina tipoInsulina,
        @DecimalMin(value = "0.5") @DecimalMax(value = "300") double unidades,
        @NotEmpty @Size(max = 12) List<@Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$",
                message = "debe tener el formato HH:mm") String> horas,
        Set<DayOfWeek> diasSemana,
        Boolean activo) {
}
