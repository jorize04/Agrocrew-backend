package pe.edu.upc.agrocrew.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agrocrew.dto.RecuperarPasswordRequestDTO;
import pe.edu.upc.agrocrew.dto.RestablecerPasswordRequestDTO;
import pe.edu.upc.agrocrew.exceptions.EnlaceExpiradoException;
import pe.edu.upc.agrocrew.exceptions.ReglaNegocioException;
import pe.edu.upc.agrocrew.models.TokenRecuperacion;
import pe.edu.upc.agrocrew.models.Usuario;
import pe.edu.upc.agrocrew.repositories.TokenRecuperacionRepository;
import pe.edu.upc.agrocrew.repositories.UsuarioRepository;
import pe.edu.upc.agrocrew.services.CorreoService;
import pe.edu.upc.agrocrew.services.RecuperacionPasswordService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Slf4j
@Service
public class RecuperacionPasswordServiceImpl implements RecuperacionPasswordService {

    static final String MENSAJE_INVALIDO = "El enlace no es válido o ya fue usado. Solicita uno nuevo.";

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final TokenRecuperacionRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final CorreoService correoService;
    private final String frontUrl;
    private final int minutosValidez;

    public RecuperacionPasswordServiceImpl(UsuarioRepository usuarioRepository,
                                           TokenRecuperacionRepository tokenRepository,
                                           PasswordEncoder passwordEncoder,
                                           CorreoService correoService,
                                           @Value("${app.front.url}") String frontUrl,
                                           @Value("${app.recuperacion.minutos-validez:30}") int minutosValidez) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.correoService = correoService;
        this.frontUrl = frontUrl.endsWith("/") ? frontUrl.substring(0, frontUrl.length() - 1) : frontUrl;
        this.minutosValidez = minutosValidez;
    }

    @Override
    @Transactional
    public void solicitar(RecuperarPasswordRequestDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();
        Optional<Usuario> encontrado = usuarioRepository.findByEmail(email);
        if (encontrado.isEmpty() || !Boolean.TRUE.equals(encontrado.get().getActivo())) {
            // Misma respuesta para no revelar qué correos están registrados.
            log.info("Recuperación solicitada para un correo sin cuenta activa");
            return;
        }
        Usuario usuario = encontrado.get();
        tokenRepository.invalidarPendientes(usuario.getId());

        String token = generarToken();
        TokenRecuperacion registro = new TokenRecuperacion();
        registro.setUsuario(usuario);
        registro.setTokenHash(hash(token));
        registro.setFechaExpiracion(LocalDateTime.now().plusMinutes(minutosValidez));
        tokenRepository.save(registro);

        String enlace = frontUrl + "/restablecer?token=" + token;
        correoService.enviarEnlaceRecuperacion(usuario.getEmail(), usuario.getNombres(), enlace, minutosValidez);
        log.info("Enlace de recuperación generado para el usuario id={}", usuario.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public void validar(String token) {
        tokenVigente(token);
    }

    @Override
    @Transactional
    public void restablecer(RestablecerPasswordRequestDTO dto) {
        TokenRecuperacion registro = tokenVigente(dto.getToken());
        Usuario usuario = registro.getUsuario();
        usuario.setPasswordHash(passwordEncoder.encode(dto.getNuevaPassword()));
        usuarioRepository.save(usuario);
        registro.setUsado(true);
        tokenRepository.save(registro);
        log.info("Contraseña restablecida para el usuario id={}", usuario.getId());
    }

    private TokenRecuperacion tokenVigente(String token) {
        if (token == null || token.isBlank()) {
            throw new ReglaNegocioException(MENSAJE_INVALIDO);
        }
        TokenRecuperacion registro = tokenRepository.findByTokenHash(hash(token.trim()))
                .orElseThrow(() -> new ReglaNegocioException(MENSAJE_INVALIDO));
        if (Boolean.TRUE.equals(registro.getUsado())) {
            throw new ReglaNegocioException(MENSAJE_INVALIDO);
        }
        if (registro.estaVencido(LocalDateTime.now())) {
            throw new EnlaceExpiradoException("El enlace expiró: solo es válido por " + minutosValidez
                    + " minutos. Solicita uno nuevo.");
        }
        return registro;
    }

    /** 32 bytes aleatorios en Base64 URL (43 caracteres), seguros para usar en el enlace. */
    private static String generarToken() {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
