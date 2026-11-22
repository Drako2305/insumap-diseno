package co.edu.ucc.insumap.dto.aplicacion;

import java.util.List;

/** Resultado de deshacer el ultimo registro (RF05). */
public record DeshacerResponse(
        AplicacionResponse aplicacionDeshecha,
        List<String> microzonasRestablecidas,
        int deshaceresDisponibles) {
}
