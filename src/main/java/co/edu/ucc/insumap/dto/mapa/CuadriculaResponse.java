package co.edu.ucc.insumap.dto.mapa;

import co.edu.ucc.insumap.modelo.enums.Lado;
import java.util.List;

/** Matriz [fila][columna] de microzonas de una zona macro en un lado del cuerpo. */
public record CuadriculaResponse(Lado lado, int filas, int columnas, List<List<MicrozonaEstadoResponse>> celdas) {
}
