package co.edu.ucc.insumap.motor.estructuras;

import co.edu.ucc.insumap.modelo.enums.Lado;
import co.edu.ucc.insumap.modelo.enums.ZonaMacro;

/**
 * Celda de aplicacion dentro de la cuadricula de una zona macro (RF02).
 * Su identificador tiene la forma {zona}_{lado}_{indice}, por ejemplo abd_izq_04.
 * El indice es 1-based y recorre la cuadricula por filas.
 */
public record Microzona(String id, ZonaMacro zonaMacro, Lado lado, int fila, int columna) {

    public static Microzona crear(ZonaMacro zona, Lado lado, int fila, int columna, int columnas) {
        return new Microzona(construirId(zona, lado, fila, columna, columnas), zona, lado, fila, columna);
    }

    public static String construirId(ZonaMacro zona, Lado lado, int fila, int columna, int columnas) {
        int indice = fila * columnas + columna + 1;
        return String.format("%s_%s_%02d", zona.getPrefijo(), lado.getPrefijo(), indice);
    }

    /** Clave de la cuadricula a la que pertenece, por ejemplo "abd_izq". */
    public String claveCuadricula() {
        return zonaMacro.getPrefijo() + "_" + lado.getPrefijo();
    }
}
