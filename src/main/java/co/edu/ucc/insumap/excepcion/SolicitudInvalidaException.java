package co.edu.ucc.insumap.excepcion;

/** Se lanza cuando los datos de la peticion son invalidos para el dominio (HTTP 400). */
public class SolicitudInvalidaException extends RuntimeException {

    public SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }
}
