package co.edu.ucc.insumap.modelo.enums;

/** Estado de recuperacion de una microzona (RF06). */
public enum Semaforo {
    /** Bloqueada: aplicacion reciente o adyacente a una aplicacion reciente. */
    ROJO,
    /** En recuperacion: se permite registrar mostrando advertencia (RF04). */
    AMARILLO,
    /** Optima: tejido recuperado y disponible. */
    VERDE
}
