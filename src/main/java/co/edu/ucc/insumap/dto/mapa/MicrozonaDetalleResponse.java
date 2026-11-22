package co.edu.ucc.insumap.dto.mapa;

import java.util.List;

/** Estado de una microzona junto con el de sus vecinas en el grafo de adyacencia. */
public record MicrozonaDetalleResponse(MicrozonaEstadoResponse microzona, List<MicrozonaEstadoResponse> vecinas) {
}
