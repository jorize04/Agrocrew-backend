package pe.edu.upc.agrocrew.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.upc.agrocrew.dto.RecuperarPasswordRequestDTO;
import pe.edu.upc.agrocrew.dto.RestablecerPasswordRequestDTO;
import pe.edu.upc.agrocrew.exceptions.EnlaceExpiradoException;
import pe.edu.upc.agrocrew.exceptions.ReglaNegocioException;
import pe.edu.upc.agrocrew.models.TokenRecuperacion;
import pe.edu.upc.agrocrew.models.Usuario;
import pe.edu.upc.agrocrew.repositories.TokenRecuperacionRepository;
import pe.edu.upc.agrocrew.repositories.UsuarioRepository;
import pe.edu.upc.agrocrew.services.impl.RecuperacionPasswordServiceImpl;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecuperacionPasswordServiceImplTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TokenRecuperacionRepository tokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private CorreoService correoService;

    private RecuperacionPasswordServiceImpl servicio;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        servicio = new RecuperacionPasswordServiceImpl(usuarioRepository, tokenRepository, passwordEncoder,
                correoService, "http://localhost:4200/", 30);
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("jose.rivera@test.com");
        usuario.setNombres("José");
        usuario.setActivo(true);
    }

    @Test
    void solicitar_correoRegistrado_guardaSoloElHashYEnviaEnlaceDe30Minutos() {
        when(usuarioRepository.findByEmail("jose.rivera@test.com")).thenReturn(Optional.of(usuario));

        RecuperarPasswordRequestDTO dto = new RecuperarPasswordRequestDTO();
        dto.setEmail(" Jose.Rivera@test.com ");
        servicio.solicitar(dto);

        ArgumentCaptor<String> enlace = ArgumentCaptor.forClass(String.class);
        verify(correoService).enviarEnlaceRecuperacion(eq("jose.rivera@test.com"), eq("José"), enlace.capture(), eq(30));
        assertTrue(enlace.getValue().startsWith("http://localhost:4200/restablecer?token="));

        ArgumentCaptor<TokenRecuperacion> guardado = ArgumentCaptor.forClass(TokenRecuperacion.class);
        verify(tokenRepository).save(guardado.capture());
        String token = enlace.getValue().substring(enlace.getValue().indexOf('=') + 1);
        assertNotEquals(token, guardado.getValue().getTokenHash());
        assertEquals(64, guardado.getValue().getTokenHash().length());
        verify(tokenRepository).invalidarPendientes(1L);
    }

    @Test
    void solicitar_correoNoRegistrado_noEnviaNadaNiRevelaError() {
        when(usuarioRepository.findByEmail("nadie@test.com")).thenReturn(Optional.empty());

        RecuperarPasswordRequestDTO dto = new RecuperarPasswordRequestDTO();
        dto.setEmail("nadie@test.com");

        assertDoesNotThrow(() -> servicio.solicitar(dto));
        verifyNoInteractions(correoService);
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void restablecer_enlaceVencido_lanza410YNoCambiaLaContrasena() {
        TokenRecuperacion vencido = token(LocalDateTime.now().minusMinutes(1), false);
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(vencido));

        assertThrows(EnlaceExpiradoException.class, () -> servicio.restablecer(request("abc")));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void restablecer_enlaceYaUsado_lanzaReglaNegocio() {
        TokenRecuperacion usado = token(LocalDateTime.now().plusMinutes(20), true);
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(usado));

        assertThrows(ReglaNegocioException.class, () -> servicio.restablecer(request("abc")));
    }

    @Test
    void restablecer_enlaceVigente_cambiaLaContrasenaYMarcaElEnlaceComoUsado() {
        TokenRecuperacion vigente = token(LocalDateTime.now().plusMinutes(20), false);
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(vigente));
        when(passwordEncoder.encode("nuevaClave123")).thenReturn("HASH");

        servicio.restablecer(request("abc"));

        assertEquals("HASH", usuario.getPasswordHash());
        assertTrue(vigente.getUsado());
        verify(usuarioRepository).save(usuario);
    }

    private TokenRecuperacion token(LocalDateTime expiracion, boolean usado) {
        TokenRecuperacion t = new TokenRecuperacion();
        t.setUsuario(usuario);
        t.setFechaExpiracion(expiracion);
        t.setUsado(usado);
        return t;
    }

    private RestablecerPasswordRequestDTO request(String token) {
        RestablecerPasswordRequestDTO dto = new RestablecerPasswordRequestDTO();
        dto.setToken(token);
        dto.setNuevaPassword("nuevaClave123");
        return dto;
    }
}
