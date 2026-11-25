package pe.edu.upc.agrocrew.exceptions;

/**
 * El enlace de restablecimiento de contraseña venció (más de 30 minutos). Se responde 410 Gone
 * para que el frontend muestre "el enlace expiró" y ofrezca pedir uno nuevo (US05, escenario 2).
 */
public class EnlaceExpiradoException extends RuntimeException {
    public EnlaceExpiradoException(String mensaje) {
        super(mensaje);
    }
}
