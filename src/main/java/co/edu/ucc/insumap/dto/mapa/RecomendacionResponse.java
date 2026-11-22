package co.edu.ucc.insumap.dto.mapa;

import co.edu.ucc.insumap.modelo.enums.ZonaMacro;
import co.edu.ucc.insumap.motor.CandidatoRecomendacion;
import co.edu.ucc.insumap.motor.Recomendacion;
import java.util.List;

/** Zona optima sugerida para la siguiente dosis y sus alternativas (RF08, RF09). */
public record RecomendacionResponse(
        MicrozonaEstadoResponse microzona,
        double puntaje,
        String motivo,
        boolean hayZonasOptimas,
        List<AlternativaResponse> alternativas,
        List<ZonaMacro> zonasSaturadas,
        List<ZonaMacro> zonasOlvidadas,
        double tiempoCalculoMs) {

    public record AlternativaResponse(MicrozonaEstadoResponse microzona, double puntaje) {

        static AlternativaResponse desde(CandidatoRecomendacion c) {
            return new AlternativaResponse(MicrozonaEstadoResponse.desde(c.estado()),
                    MicrozonaEstadoResponse.redondear(c.puntaje()));
        }
    }

    public static RecomendacionResponse desde(Recomendacion r) {
        return new RecomendacionResponse(MicrozonaEstadoResponse.desde(r.principal().estado()),
                MicrozonaEstadoResponse.redondear(r.principal().puntaje()), r.motivo(), r.hayZonasOptimas(),
                r.alternativas().stream().map(AlternativaResponse::desde).toList(), r.zonasSaturadas(),
                r.zonasOlvidadas(), Math.round(r.tiempoCalculoMs() * 1000.0) / 1000.0);
    }
}
