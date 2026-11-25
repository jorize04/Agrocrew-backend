package pe.edu.upc.agrocrew.services;

/**
 * Envío de correos de la plataforma.
 */
public interface CorreoService {

    /** Envía el enlace para restablecer la contraseña (US05). */
    void enviarEnlaceRecuperacion(String email, String nombres, String enlace, int minutosValidez);
}
