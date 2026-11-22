package co.edu.ucc.insumap.dto.paciente;

import co.edu.ucc.insumap.modelo.MedicoTratante;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MedicoTratanteDto(
        @NotBlank @Size(max = 120) String nombre,
        @Size(max = 80) String especialidad,
        @Email @Size(max = 120) String email,
        @Size(max = 20) String telefono) {

    public static MedicoTratanteDto desde(MedicoTratante m) {
        return m == null ? null : new MedicoTratanteDto(m.getNombre(), m.getEspecialidad(), m.getEmail(), m.getTelefono());
    }

    public MedicoTratante aEntidad() {
        return new MedicoTratante(nombre, especialidad, email, telefono);
    }
}
