package co.edu.ucc.insumap.excepcion;

/** Se lanza cuando un paciente, aplicacion, cronograma, recordatorio o microzona no existe (HTTP 404). */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String recurso, String id) {
        super("No existe " + recurso.toLowerCase() + " con id " + id);
    }
}
