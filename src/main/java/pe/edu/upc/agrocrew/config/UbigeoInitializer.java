package pe.edu.upc.agrocrew.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agrocrew.models.Departamento;
import pe.edu.upc.agrocrew.models.Distrito;
import pe.edu.upc.agrocrew.models.Provincia;
import pe.edu.upc.agrocrew.repositories.DepartamentoRepository;
import pe.edu.upc.agrocrew.repositories.DistritoRepository;
import pe.edu.upc.agrocrew.repositories.ProvinciaRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Carga departamentos, provincias y distritos desde resources/data/ubigeo_distritos.csv
 * solo la primera vez (cuando la tabla departamentos está vacía).
 * Fuente: INEI, vía el dataset abierto "ubigeo-peru-aumentado" (github.com/jmcastagnetto/ubigeo-peru-aumentado).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UbigeoInitializer implements CommandLineRunner {

    private static final String ARCHIVO = "data/ubigeo_distritos.csv";

    private final DepartamentoRepository departamentoRepository;
    private final ProvinciaRepository provinciaRepository;
    private final DistritoRepository distritoRepository;

    @Override
    @Transactional
    public void run(String... args) throws IOException {
        if (departamentoRepository.count() > 0) {
            return;
        }
        log.info("Cargando ubigeo del Perú (solo la primera vez, puede tardar unos segundos)...");

        Map<String, Departamento> departamentos = new LinkedHashMap<>();
        Map<String, Provincia> provincias = new LinkedHashMap<>();
        int totalDistritos = 0;

        try (BufferedReader lector = new BufferedReader(new InputStreamReader(
                new ClassPathResource(ARCHIVO).getInputStream(), StandardCharsets.UTF_8))) {

            lector.readLine(); // encabezado
            String linea;
            while ((linea = lector.readLine()) != null) {
                if (linea.isBlank()) {
                    continue;
                }
                // ubigeo,departamento,provincia,distrito,latitud,longitud,altitud_msnm
                String[] c = linea.split(",", -1);
                String ubigeo = c[0];
                if (!ubigeo.matches("\\d{6}")) {
                    log.warn("Fila de ubigeo omitida por código inválido: {}", linea);
                    continue;
                }

                Departamento dep = departamentos.computeIfAbsent(ubigeo.substring(0, 2), cod -> {
                    Departamento d = new Departamento();
                    d.setUbigeo(cod);
                    d.setNombre(c[1]);
                    return departamentoRepository.save(d);
                });

                Provincia prov = provincias.computeIfAbsent(ubigeo.substring(0, 4), cod -> {
                    Provincia p = new Provincia();
                    p.setUbigeo(cod);
                    p.setNombre(c[2]);
                    p.setDepartamento(dep);
                    return provinciaRepository.save(p);
                });

                Distrito dist = new Distrito();
                dist.setUbigeo(ubigeo);
                dist.setNombre(c[3]);
                dist.setProvincia(prov);
                dist.setLatitud(aDouble(c[4]));
                dist.setLongitud(aDouble(c[5]));
                dist.setAltitudMsnm(c[6].isBlank() ? null : Integer.valueOf(c[6]));
                distritoRepository.save(dist);
                totalDistritos++;
            }
        }
        log.info("Ubigeo cargado: {} departamentos, {} provincias, {} distritos",
                departamentos.size(), provincias.size(), totalDistritos);
    }

    private Double aDouble(String valor) {
        return valor.isBlank() ? null : Double.valueOf(valor);
    }
}
