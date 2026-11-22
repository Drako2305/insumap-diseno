package co.edu.ucc.insumap.dto.paciente;

import co.edu.ucc.insumap.modelo.Paciente;
import co.edu.ucc.insumap.modelo.enums.TipoDiabetes;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

public record PacienteResponse(
        String id,
        String nombres,
        String apellidos,
        String nombreCompleto,
        String documentoIdentidad,
        LocalDate fechaNacimiento,
        int edad,
        TipoDiabetes tipoDiabetes,
        String email,
        String telefono,
        ConfiguracionMapaResponse configuracionMapa,
        List<CuidadorDto> cuidadores,
        MedicoTratanteDto medicoTratante,
        int deshaceresDisponibles,
        Instant fechaRegistro) {

    public static PacienteResponse desde(Paciente p, LocalDate hoy) {
        return new PacienteResponse(p.getId(), p.getNombres(), p.getApellidos(), p.nombreCompleto(),
                p.getDocumentoIdentidad(), p.getFechaNacimiento(),
                Period.between(p.getFechaNacimiento(), hoy).getYears(), p.getTipoDiabetes(), p.getEmail(),
                p.getTelefono(), ConfiguracionMapaResponse.desde(p.getConfiguracionMapa()),
                p.getCuidadores().stream().map(CuidadorDto::desde).toList(),
                MedicoTratanteDto.desde(p.getMedicoTratante()), p.getPilaDeshacer().size(), p.getFechaRegistro());
    }
}
