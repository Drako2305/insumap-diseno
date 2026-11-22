package co.edu.ucc.insumap.dto.mapa;

import co.edu.ucc.insumap.modelo.enums.Semaforo;
import co.edu.ucc.insumap.motor.estructuras.MapaEstados;

public record ResumenSemaforoResponse(long rojo, long amarillo, long verde, int total) {

    public static ResumenSemaforoResponse desde(MapaEstados estados) {
        return new ResumenSemaforoResponse(estados.contar(Semaforo.ROJO), estados.contar(Semaforo.AMARILLO),
                estados.contar(Semaforo.VERDE), estados.tamano());
    }
}
