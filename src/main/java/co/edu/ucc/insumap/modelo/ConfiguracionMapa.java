package co.edu.ucc.insumap.modelo;

/**
 * Tamano configurable de la cuadricula que subdivide cada zona macro y lado (RF02).
 * Se embebe dentro del documento del paciente.
 */
public class ConfiguracionMapa {

    private int filas;
    private int columnas;

    public ConfiguracionMapa() {
    }

    public ConfiguracionMapa(int filas, int columnas) {
        this.filas = filas;
        this.columnas = columnas;
    }

    /** Cantidad total de microzonas del mapa: 4 zonas x 2 lados x filas x columnas. */
    public int totalMicrozonas() {
        return 8 * filas * columnas;
    }

    public int getFilas() {
        return filas;
    }

    public void setFilas(int filas) {
        this.filas = filas;
    }

    public int getColumnas() {
        return columnas;
    }

    public void setColumnas(int columnas) {
        this.columnas = columnas;
    }
}
