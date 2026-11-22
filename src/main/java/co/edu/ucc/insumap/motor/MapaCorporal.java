package co.edu.ucc.insumap.motor;

import co.edu.ucc.insumap.modelo.ConfiguracionMapa;
import co.edu.ucc.insumap.modelo.enums.Lado;
import co.edu.ucc.insumap.modelo.enums.ZonaMacro;
import co.edu.ucc.insumap.motor.estructuras.CuadriculaZona;
import co.edu.ucc.insumap.motor.estructuras.GrafoAdyacencia;
import co.edu.ucc.insumap.motor.estructuras.Microzona;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Mapa corporal 2D de un paciente (RF01, RF02): 4 zonas macro x 2 lados, cada una con su cuadricula.
 * Contiene la tabla hash de microzonas (consulta O(1) por id) y el grafo de adyacencia (RF07).
 *
 * <p>Convencion de adyacencia: dentro de una cuadricula se conectan los 8 vecinos (horizontal,
 * vertical y diagonal). En el abdomen, la ultima columna del lado izquierdo y la primera del lado
 * derecho estan sobre la linea media (ombligo), por lo que tambien se conectan entre si.</p>
 */
public class MapaCorporal {

    private final int filas;
    private final int columnas;
    private final Map<String, CuadriculaZona> cuadriculas = new LinkedHashMap<>();
    private final Map<String, Microzona> indice = new HashMap<>();
    private final GrafoAdyacencia grafo = new GrafoAdyacencia();

    public MapaCorporal(ConfiguracionMapa configuracion) {
        this.filas = configuracion.getFilas();
        this.columnas = configuracion.getColumnas();
        for (ZonaMacro zona : ZonaMacro.values()) {
            for (Lado lado : Lado.values()) {
                CuadriculaZona cuadricula = new CuadriculaZona(zona, lado, filas, columnas);
                cuadriculas.put(clave(zona, lado), cuadricula);
                for (Microzona microzona : cuadricula.todas()) {
                    indice.put(microzona.id(), microzona);
                    grafo.agregarNodo(microzona.id());
                }
                conectarVecinosInternos(cuadricula);
            }
        }
        conectarLineaMediaAbdomen();
    }

    private void conectarVecinosInternos(CuadriculaZona cuadricula) {
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                Microzona actual = cuadricula.obtener(f, c);
                for (int df = -1; df <= 1; df++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        Microzona vecina = cuadricula.obtener(f + df, c + dc);
                        if (vecina != null && vecina != actual) {
                            grafo.agregarArista(actual.id(), vecina.id());
                        }
                    }
                }
            }
        }
    }

    private void conectarLineaMediaAbdomen() {
        CuadriculaZona izquierda = cuadricula(ZonaMacro.ABDOMEN, Lado.IZQUIERDO);
        CuadriculaZona derecha = cuadricula(ZonaMacro.ABDOMEN, Lado.DERECHO);
        for (int f = 0; f < filas; f++) {
            Microzona borde = izquierda.obtener(f, columnas - 1);
            for (int df = -1; df <= 1; df++) {
                Microzona vecina = derecha.obtener(f + df, 0);
                if (vecina != null) {
                    grafo.agregarArista(borde.id(), vecina.id());
                }
            }
        }
    }

    private static String clave(ZonaMacro zona, Lado lado) {
        return zona.getPrefijo() + "_" + lado.getPrefijo();
    }

    /** Busqueda O(1) de una microzona por su identificador. */
    public Optional<Microzona> buscar(String microzonaId) {
        return Optional.ofNullable(microzonaId == null ? null : indice.get(microzonaId.toLowerCase()));
    }

    public boolean existe(String microzonaId) {
        return buscar(microzonaId).isPresent();
    }

    public Set<String> vecinos(String microzonaId) {
        return grafo.vecinos(microzonaId);
    }

    public CuadriculaZona cuadricula(ZonaMacro zona, Lado lado) {
        return cuadriculas.get(clave(zona, lado));
    }

    public Collection<CuadriculaZona> cuadriculas() {
        return cuadriculas.values();
    }

    public List<Microzona> todas() {
        List<Microzona> lista = new ArrayList<>(indice.size());
        cuadriculas.values().forEach(c -> lista.addAll(c.todas()));
        return lista;
    }

    public GrafoAdyacencia getGrafo() {
        return grafo;
    }

    public int getFilas() {
        return filas;
    }

    public int getColumnas() {
        return columnas;
    }

    public int totalMicrozonas() {
        return indice.size();
    }
}
