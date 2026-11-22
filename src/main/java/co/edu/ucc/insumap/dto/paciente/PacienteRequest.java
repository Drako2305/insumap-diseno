package co.edu.ucc.insumap.dto.paciente;

import co.edu.ucc.insumap.modelo.enums.TipoDiabetes;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/** Datos para crear o actualizar un paciente. */
public record PacienteRequest(
        @NotBlank @Size(max = 80) String nombres,
        @NotBlank @Size(max = 80) String apellidos,
        @NotBlank @Size(max = 20) String documentoIdentidad,
        @NotNull @Past LocalDate fechaNacimiento,
        @NotNull TipoDiabetes tipoDiabetes,
        @Email @Size(max = 120) String email,
        @Size(max = 20) String telefono,
        @Valid ConfiguracionMapaRequest configuracionMapa,
        @Valid @Size(max = 5) List<CuidadorDto> cuidadores,
        @Valid MedicoTratanteDto medicoTratante) {
}
