package pe.edu.upc.agrocrew.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utilitario para obtener el email del usuario autenticado desde cualquier servicio.
 * Ejemplo: String email = UsuarioActual.email();
 */
public final class UsuarioActual {

    private UsuarioActual() {
    }

    public static String email() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    public static boolean tieneRol(String rol) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + rol));
    }
}
