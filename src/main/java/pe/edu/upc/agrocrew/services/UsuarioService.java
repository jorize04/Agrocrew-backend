package pe.edu.upc.agrocrew.services;

import pe.edu.upc.agrocrew.dto.PerfilRequestDTO;
import pe.edu.upc.agrocrew.dto.UsuarioResponseDTO;
import pe.edu.upc.agrocrew.models.Usuario;

import java.util.List;

public interface UsuarioService {

    /** Entidad del usuario autenticado; úsenla en otros servicios (ej. para asignar el dueño de un predio). */
    Usuario obtenerUsuarioActual();

    UsuarioResponseDTO obtenerPerfilActual();

    UsuarioResponseDTO actualizarPerfil(PerfilRequestDTO dto);

    UsuarioResponseDTO convertirADTO(Usuario usuario);

    // ---- Administración (US33)

    List<UsuarioResponseDTO> listarTodos();

    UsuarioResponseDTO cambiarRol(Long usuarioId, String rol);

    UsuarioResponseDTO cambiarEstado(Long usuarioId, boolean activo);
}
