package co.edu.ucc.insumap.motor;

import co.edu.ucc.insumap.modelo.enums.ZonaMacro;
import java.util.List;

/**
 * Sugerencia del motor para la siguiente dosis.
 *
 * @param principal         microzona optima
 * @param alternativas      siguientes mejores opciones (extraidas del Max-Heap)
 * @param zonasSaturadas    zonas macro penalizadas
 * @param zonasOlvidadas    zonas macro priorizadas
 * @param hayZonasOptimas   false si ninguna microzona esta en VERDE
 * @param motivo            explicacion legible de la sugerencia
 * @param tiempoCalculoMs   tiempo de calculo (requisito no funcional: menor a 200 ms)
 */
public record Recomendacion(
        CandidatoRecomendacion principal,
        List<CandidatoRecomendacion> alternativas,
        List<ZonaMacro> zonasSaturadas,
        List<ZonaMacro> zonasOlvidadas,
        boolean hayZonasOptimas,
        String motivo,
        double tiempoCalculoMs) {
}
