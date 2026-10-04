package pe.edu.upc.agrocrew.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ajustes de esquema que ddl-auto=update no hace solo.
 * Hibernate crea una restricción CHECK con los valores del enum ServicioExterno. Al agregar el valor IA,
 * las bases creadas antes conservan la restricción vieja y rechazarían los registros de la IA.
 * Se elimina la restricción (el valor ya lo controla el enum en Java). Es seguro ejecutarlo varias veces.
 */
@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class AjustesBaseDatos implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.execute("ALTER TABLE registros_integracion DROP CONSTRAINT IF EXISTS registros_integracion_servicio_check");
        } catch (Exception e) {
            log.warn("No se pudo ajustar la restricción de registros_integracion: {}", e.getMessage());
        }
    }
}
