package pe.edu.upc.agrocrew.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agrocrew.models.Rol;
import pe.edu.upc.agrocrew.models.Usuario;
import pe.edu.upc.agrocrew.repositories.RolRepository;
import pe.edu.upc.agrocrew.repositories.UsuarioRepository;

import java.util.List;

/** Crea los roles base y, si se configuró, el usuario administrador inicial. Es idempotente. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        for (String nombre : List.of(Rol.PRODUCTOR, Rol.ASESOR, Rol.ADMIN)) {
            if (!rolRepository.existsByNombre(nombre)) {
                rolRepository.save(new Rol(nombre));
                log.info("Rol creado: {}", nombre);
            }
        }

        if (!adminEmail.isBlank() && !adminPassword.isBlank()
                && !usuarioRepository.existsByEmail(adminEmail.toLowerCase())) {
            Usuario admin = new Usuario();
            admin.setNombres("Administrador");
            admin.setApellidos("AgroCrew");
            admin.setEmail(adminEmail.toLowerCase());
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setRol(rolRepository.findByNombre(Rol.ADMIN).orElseThrow());
            usuarioRepository.save(admin);
            log.info("Usuario administrador inicial creado");
        }
    }
}
