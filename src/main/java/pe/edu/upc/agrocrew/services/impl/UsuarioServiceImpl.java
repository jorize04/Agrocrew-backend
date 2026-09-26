package pe.edu.upc.agrocrew.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agrocrew.dto.PerfilRequestDTO;
import pe.edu.upc.agrocrew.dto.UsuarioResponseDTO;
import pe.edu.upc.agrocrew.exceptions.RecursoNoEncontradoException;
import pe.edu.upc.agrocrew.exceptions.ReglaNegocioException;
import pe.edu.upc.agrocrew.models.Rol;
import pe.edu.upc.agrocrew.models.Usuario;
import pe.edu.upc.agrocrew.repositories.RolRepository;
import pe.edu.upc.agrocrew.repositories.UsuarioRepository;
import pe.edu.upc.agrocrew.security.UsuarioActual;
import pe.edu.upc.agrocrew.services.UsuarioService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerUsuarioActual() {
        String email = UsuarioActual.email();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPerfilActual() {
        return convertirADTO(obtenerUsuarioActual());
    }

    @Override
    @Transactional
    public UsuarioResponseDTO actualizarPerfil(PerfilRequestDTO dto) {
        Usuario u = obtenerUsuarioActual();
        u.setNombres(dto.getNombres().trim());
        u.setApellidos(dto.getApellidos().trim());
        u.setTelefono(dto.getTelefono());
        u.setOrganizacion(dto.getOrganizacion());
        log.info("Perfil actualizado usuario={}", u.getId());
        return convertirADTO(usuarioRepository.save(u));
    }

    @Override
    public UsuarioResponseDTO convertirADTO(Usuario u) {
        return new UsuarioResponseDTO(u.getId(), u.getNombres(), u.getApellidos(), u.getEmail(),
                u.getTelefono(), u.getOrganizacion(), u.getRol().getNombre(), u.getActivo(),
                u.getFechaRegistro());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAllByOrderByFechaRegistroDesc().stream().map(this::convertirADTO).toList();
    }

    @Override
    @Transactional
    public UsuarioResponseDTO cambiarRol(Long usuarioId, String nombreRol) {
        Usuario u = buscarOtroUsuario(usuarioId, "cambiar su propio rol");
        Rol rol = rolRepository.findByNombre(nombreRol)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado: " + nombreRol));
        u.setRol(rol);
        log.info("Rol del usuario {} cambiado a {}", usuarioId, nombreRol);
        return convertirADTO(usuarioRepository.save(u));
    }

    @Override
    @Transactional
    public UsuarioResponseDTO cambiarEstado(Long usuarioId, boolean activo) {
        Usuario u = buscarOtroUsuario(usuarioId, "desactivar su propia cuenta");
        u.setActivo(activo);
        log.warn("Usuario {} {}", usuarioId, activo ? "activado" : "desactivado");
        return convertirADTO(usuarioRepository.save(u));
    }

    /** El administrador no puede modificarse a sí mismo para no quedarse sin acceso. */
    private Usuario buscarOtroUsuario(Long usuarioId, String accion) {
        Usuario u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        if (u.getEmail().equals(UsuarioActual.email())) {
            throw new ReglaNegocioException("Un administrador no puede " + accion);
        }
        return u;
    }
}
