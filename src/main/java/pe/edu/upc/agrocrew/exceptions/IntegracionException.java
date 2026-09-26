package pe.edu.upc.agrocrew.exceptions;

/** Un servicio externo (MIDAGRI, ANA, Open-Meteo) no respondió o respondió con error. Se traduce a HTTP 503. */
public class IntegracionException extends RuntimeException {
    public IntegracionException(String mensaje) {
        super(mensaje);
    }
}
