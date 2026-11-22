package co.edu.ucc.insumap.dto.mapa;

import co.edu.ucc.insumap.modelo.enums.ZonaMacro;
import java.util.List;

public record ZonaMacroResponse(
        ZonaMacro zona,
        String nombre,
        int usosEnVentana,
        double diasSinUso,
        boolean saturada,
        boolean olvidada,
        double factorPrioridad,
        List<CuadriculaResponse> lados) {
}
