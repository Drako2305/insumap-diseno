package co.edu.ucc.insumap.modelo.enums;

/**
 * Zonas macro anatomicas del mapa corporal 2D (RF01).
 * Cada zona tiene un prefijo usado para construir el identificador de la microzona (ej. abd_izq_04).
 */
public enum ZonaMacro {
    ABDOMEN("abd", "Abdomen"),
    MUSLOS("mus", "Muslos"),
    BRAZOS("bra", "Brazos"),
    GLUTEOS("glu", "Gluteos");

    private final String prefijo;
    private final String nombre;

    ZonaMacro(String prefijo, String nombre) {
        this.prefijo = prefijo;
        this.nombre = nombre;
    }

    public String getPrefijo() {
        return prefijo;
    }

    public String getNombre() {
        return nombre;
    }

    public static ZonaMacro desdePrefijo(String prefijo) {
        for (ZonaMacro zona : values()) {
            if (zona.prefijo.equalsIgnoreCase(prefijo)) {
                return zona;
            }
        }
        throw new IllegalArgumentException("Prefijo de zona desconocido: " + prefijo);
    }
}
