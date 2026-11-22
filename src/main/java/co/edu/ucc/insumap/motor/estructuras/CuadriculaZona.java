package co.edu.ucc.insumap.motor.estructuras;

import co.edu.ucc.insumap.modelo.enums.Lado;
import co.edu.ucc.insumap.modelo.enums.ZonaMacro;
import java.util.ArrayList;
import java.util.List;

/**
 * Matriz bidimensional (2D array) que representa la cuadricula de microzonas de una zona macro
 * en un lado del cuerpo (RF01, RF02).
 */
public class CuadriculaZona {

    private final ZonaMacro zonaMacro;
    private final Lado lado;
    private final Microzona[][] celdas;

    public CuadriculaZona(ZonaMacro zonaMacro, Lado lado, int filas, int columnas) {
        if (filas <= 0 || columnas <= 0) {
            throw new IllegalArgumentException("La cuadricula debe tener al menos una fila y una columna");
        }
        this.zonaMacro = zonaMacro;
        this.lado = lado;
        this.celdas = new Microzona[filas][columnas];
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                celdas[f][c] = Microzona.crear(zonaMacro, lado, f, c, columnas);
            }
        }
    }

    public Microzona obtener(int fila, int columna) {
        return enRango(fila, columna) ? celdas[fila][columna] : null;
    }

    public boolean enRango(int fila, int columna) {
        return fila >= 0 && fila < getFilas() && columna >= 0 && columna < getColumnas();
    }

    /** Recorre la matriz por filas y devuelve todas sus microzonas. */
    public List<Microzona> todas() {
        List<Microzona> lista = new ArrayList<>(getFilas() * getColumnas());
        for (Microzona[] fila : celdas) {
            for (Microzona celda : fila) {
                lista.add(celda);
            }
        }
        return lista;
    }

    public ZonaMacro getZonaMacro() {
        return zonaMacro;
    }

    public Lado getLado() {
        return lado;
    }

    public int getFilas() {
        return celdas.length;
    }

    public int getColumnas() {
        return celdas[0].length;
    }
}
