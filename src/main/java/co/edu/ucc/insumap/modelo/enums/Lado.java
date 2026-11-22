package co.edu.ucc.insumap.modelo.enums;

/** Lado del cuerpo en el que se ubica la cuadricula de una zona macro. */
public enum Lado {
    IZQUIERDO("izq"),
    DERECHO("der");

    private final String prefijo;

    Lado(String prefijo) {
        this.prefijo = prefijo;
    }

    public String getPrefijo() {
        return prefijo;
    }

    public static Lado desdePrefijo(String prefijo) {
        for (Lado lado : values()) {
            if (lado.prefijo.equalsIgnoreCase(prefijo)) {
                return lado;
            }
        }
        throw new IllegalArgumentException("Prefijo de lado desconocido: " + prefijo);
    }
}
