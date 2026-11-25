package pe.edu.upc.agrocrew.services;

import pe.edu.upc.agrocrew.dto.RecuperarPasswordRequestDTO;
import pe.edu.upc.agrocrew.dto.RestablecerPasswordRequestDTO;

/**
 * Recuperación de contraseña con enlace de un solo uso válido por 30 minutos (US05).
 */
public interface RecuperacionPasswordService {

    /** Genera el enlace y lo envía por correo. No revela si el correo está registrado. */
    void solicitar(RecuperarPasswordRequestDTO dto);

    /** Comprueba el enlace: 400 si no es válido o ya se usó, 410 si venció. */
    void validar(String token);

    /** Cambia la contraseña y deja el enlace como usado. */
    void restablecer(RestablecerPasswordRequestDTO dto);
}
