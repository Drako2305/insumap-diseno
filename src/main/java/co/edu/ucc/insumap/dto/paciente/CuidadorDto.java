package co.edu.ucc.insumap.dto.paciente;

import co.edu.ucc.insumap.modelo.Cuidador;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CuidadorDto(
        @NotBlank @Size(max = 120) String nombre,
        @Size(max = 40) String parentesco,
        @Size(max = 20) String telefono,
        @Email @Size(max = 120) String email) {

    public static CuidadorDto desde(Cuidador c) {
        return new CuidadorDto(c.getNombre(), c.getParentesco(), c.getTelefono(), c.getEmail());
    }

    public Cuidador aEntidad() {
        return new Cuidador(nombre, parentesco, telefono, email);
    }
}
