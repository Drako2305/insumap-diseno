package co.edu.ucc.insumap.dto.aplicacion;

import java.util.List;

/**
 * Resultado de registrar una inyeccion.
 *
 * @param advertencia          mensaje no bloqueante si la microzona no estaba en VERDE (RF04)
 * @param microzonasBloqueadas la microzona usada y sus adyacentes, ahora en ROJO (RF07)
 */
public record RegistroAplicacionResponse(
        AplicacionResponse aplicacion,
        String advertencia,
        List<String> microzonasBloqueadas,
        int deshaceresDisponibles) {
}
