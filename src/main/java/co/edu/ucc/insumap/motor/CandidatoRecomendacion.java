package co.edu.ucc.insumap.motor;

/**
 * Microzona candidata a recibir la siguiente dosis, con su puntaje heuristico (RF09).
 * Es el elemento que se ordena dentro del Max-Heap del motor.
 */
public record CandidatoRecomendacion(EstadoMicrozona estado, double puntaje) {

    /** Nivel de prioridad segun semaforo: VERDE (2) > AMARILLO (1) > ROJO (0). */
    public int nivel() {
        return switch (estado.getSemaforo()) {
            case VERDE -> 2;
            case AMARILLO -> 1;
            case ROJO -> 0;
        };
    }
}
