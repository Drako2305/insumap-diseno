package co.edu.ucc.insumap.motor.estructuras;

import co.edu.ucc.insumap.modelo.enums.Semaforo;
import co.edu.ucc.insumap.motor.EstadoMicrozona;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Tabla hash (Hash Map) que asocia el identificador de cada microzona (ej. abd_izq_04) con su
 * estado de disponibilidad. Consultar y actualizar un estado cuesta O(1).
 */
public class MapaEstados {

    private final Map<String, EstadoMicrozona> estados = new HashMap<>();

    public void actualizar(EstadoMicrozona estado) {
        estados.put(estado.getMicrozona().id(), estado);
    }

    public Optional<EstadoMicrozona> obtener(String microzonaId) {
        return Optional.ofNullable(estados.get(microzonaId));
    }

    public Collection<EstadoMicrozona> todos() {
        return estados.values();
    }

    public long contar(Semaforo semaforo) {
        return estados.values().stream().filter(e -> e.getSemaforo() == semaforo).count();
    }

    public int tamano() {
        return estados.size();
    }
}
