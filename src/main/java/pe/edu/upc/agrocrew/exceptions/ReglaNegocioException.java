package pe.edu.upc.agrocrew.exceptions;

/** Se traduce a HTTP 400 cuando los datos son válidos en formato pero violan una regla del negocio. */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
