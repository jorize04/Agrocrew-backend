package pe.edu.upc.agrocrew.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agrocrew.dto.AuthResponseDTO;
import pe.edu.upc.agrocrew.dto.LoginRequestDTO;
import pe.edu.upc.agrocrew.dto.RegistroRequestDTO;
import pe.edu.upc.agrocrew.dto.UsuarioResponseDTO;
import pe.edu.upc.agrocrew.exceptions.ConflictoException;
import pe.edu.upc.agrocrew.exceptions.RecursoNoEncontradoException;
import pe.edu.upc.agrocrew.models.Rol;
import pe.edu.upc.agrocrew.models.Usuario;
import pe.edu.upc.agrocrew.repositories.RolRepository;
import pe.edu.upc.agrocrew.repositories.UsuarioRepository;
import pe.edu.upc.agrocrew.security.JwtUtil;
import pe.edu.upc.agrocrew.services.AuthService;
import pe.edu.upc.agrocrew.services.UsuarioService;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsuarioService usuarioService;

    @Override
    @Transactional
    public UsuarioResponseDTO registrar(RegistroRequestDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new ConflictoException("El correo ya está registrado");
        }
        Rol rol = rolRepository.findByNombre(dto.getTipoUsuario())
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado: " + dto.getTipoUsuario()));

        Usuario usuario = new Usuario();
        usuario.setNombres(dto.getNombres().trim());
        usuario.setApellidos(dto.getApellidos().trim());
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        usuario.setTelefono(dto.getTelefono());
        usuario.setOrganizacion(dto.getOrganizacion());
        usuario.setRol(rol);

        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario registrado id={} rol={}", guardado.getId(), rol.getNombre());
        return usuarioService.convertirADTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();
        // Lanza BadCredentialsException o DisabledException; GlobalExceptionHandler las traduce a 401/403.
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, dto.getPassword()));

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        String token = jwtUtil.generarToken(usuario.getEmail(), usuario.getRol().getNombre());
        log.info("Inicio de sesión id={}", usuario.getId());
        return new AuthResponseDTO(token, "Bearer", jwtUtil.getExpiracionMs(), usuarioService.convertirADTO(usuario));
    }
}
