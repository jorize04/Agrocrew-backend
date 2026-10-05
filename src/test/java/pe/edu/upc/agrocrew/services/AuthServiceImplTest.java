package pe.edu.upc.agrocrew.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.upc.agrocrew.dto.RegistroRequestDTO;
import pe.edu.upc.agrocrew.dto.UsuarioResponseDTO;
import pe.edu.upc.agrocrew.exceptions.ConflictoException;
import pe.edu.upc.agrocrew.models.Rol;
import pe.edu.upc.agrocrew.models.Usuario;
import pe.edu.upc.agrocrew.repositories.RolRepository;
import pe.edu.upc.agrocrew.repositories.UsuarioRepository;
import pe.edu.upc.agrocrew.security.JwtUtil;
import pe.edu.upc.agrocrew.services.impl.AuthServiceImpl;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RolRepository rolRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;
    @Mock private UsuarioService usuarioService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegistroRequestDTO dto;

    @BeforeEach
    void setUp() {
        dto = new RegistroRequestDTO();
        dto.setNombres("José");
        dto.setApellidos("Rivera");
        dto.setEmail("  Jose.Rivera@Test.com ");
        dto.setPassword("clave1234");
        dto.setTipoUsuario(Rol.PRODUCTOR);
    }

    @Test
    void registrar_conCorreoNuevo_guardaUsuarioConPasswordCifradaYEmailNormalizado() {
        Rol rol = new Rol(1L, Rol.PRODUCTOR);
        when(usuarioRepository.existsByEmail("jose.rivera@test.com")).thenReturn(false);
        when(rolRepository.findByNombre(Rol.PRODUCTOR)).thenReturn(Optional.of(rol));
        when(passwordEncoder.encode("clave1234")).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioService.convertirADTO(any(Usuario.class))).thenReturn(new UsuarioResponseDTO());

        authService.registrar(dto);

        verify(usuarioRepository).save(argThat(u ->
                u.getEmail().equals("jose.rivera@test.com")
                        && u.getPasswordHash().equals("hash")
                        && u.getRol() == rol));
    }

    @Test
    void registrar_conCorreoExistente_lanzaConflicto() {
        when(usuarioRepository.existsByEmail("jose.rivera@test.com")).thenReturn(true);

        ConflictoException ex = assertThrows(ConflictoException.class, () -> authService.registrar(dto));

        assertEquals("El correo ya está registrado", ex.getMessage());
        verify(usuarioRepository, never()).save(any());
    }
}
