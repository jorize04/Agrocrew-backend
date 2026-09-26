package pe.edu.upc.agrocrew.exceptions;

/** Se traduce a HTTP 409 (por ejemplo, un correo o nombre ya registrado). */
public class ConflictoException extends RuntimeException {
    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
