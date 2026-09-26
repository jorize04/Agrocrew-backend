package pe.edu.upc.agrocrew.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agrocrew.models.*;
import pe.edu.upc.agrocrew.repositories.CultivoRepository;
import pe.edu.upc.agrocrew.repositories.GrupoCumRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Carga los 5 grupos CUM y los cultivos iniciales (resources/data/cultivos.csv)
 * solo si las tablas están vacías. Para cambiar un cultivo después de la primera carga,
 * usen los endpoints de administración.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogoInitializer implements CommandLineRunner {

    private static final String ARCHIVO_CULTIVOS = "data/cultivos.csv";

    private final GrupoCumRepository grupoCumRepository;
    private final CultivoRepository cultivoRepository;

    @Override
    @Transactional
    public void run(String... args) throws IOException {
        if (grupoCumRepository.count() == 0) {
            cargarGruposCum();
        }
        if (cultivoRepository.count() == 0) {
            cargarCultivos();
        }
    }

    private void cargarGruposCum() {
        grupo("A", "Tierras aptas para cultivo en limpio",
                "Son las mejores tierras para sembrar. Permiten remover el suelo y sembrar cultivos de temporada (papa, maíz, quinua, hortalizas) sin dañar el terreno, si se trabajan con buenas prácticas.",
                "Cultivos de temporada, cultivos permanentes, pastos y forestales.",
                "Ninguno en especial; conviene evitar el sobrepastoreo y la quema.");
        grupo("C", "Tierras aptas para cultivos permanentes",
                "Tienen alguna limitación (pendiente, suelo o clima) que hace riesgoso remover el suelo cada campaña, pero sí permiten cultivos que se quedan varios años, como frutales, café o cacao.",
                "Frutales, café, cacao, pastos y forestales.",
                "Cultivos de temporada que obligan a arar el suelo cada año, porque aceleran la erosión.");
        grupo("P", "Tierras aptas para pastos",
                "No son adecuadas para sembrar cultivos, pero sí para mantener pastos naturales o cultivados para alimentar ganado.",
                "Pastos naturales o cultivados, forrajes y forestales.",
                "Cultivos de temporada y cultivos permanentes.");
        grupo("F", "Tierras aptas para producción forestal",
                "Por su pendiente o suelo, solo son adecuadas para árboles. Sembrar cultivos aquí suele terminar en erosión y pérdida del terreno.",
                "Plantaciones forestales y manejo del bosque.",
                "Cultivos agrícolas y pastoreo intensivo.");
        grupo("X", "Tierras de protección",
                "Tienen limitaciones muy fuertes (pendientes extremas, suelos muy delgados, riesgo alto). Deben conservarse sin uso agrícola ni forestal productivo.",
                "Conservación, protección de fuentes de agua, turismo de naturaleza.",
                "Cualquier cultivo, pastoreo o tala.");
        log.info("Grupos CUM cargados: 5");
    }

    private void grupo(String codigo, String nombre, String descripcion, String recomendados, String noRecomendados) {
        GrupoCum g = new GrupoCum();
        g.setCodigo(codigo);
        g.setNombre(nombre);
        g.setDescripcionSimple(descripcion);
        g.setUsosRecomendados(recomendados);
        g.setUsosNoRecomendados(noRecomendados);
        grupoCumRepository.save(g);
    }

    private void cargarCultivos() throws IOException {
        int total = 0;
        try (BufferedReader lector = new BufferedReader(new InputStreamReader(
                new ClassPathResource(ARCHIVO_CULTIVOS).getInputStream(), StandardCharsets.UTF_8))) {

            lector.readLine(); // encabezado
            String linea;
            while ((linea = lector.readLine()) != null) {
                if (linea.isBlank() || linea.startsWith("#")) {
                    continue;
                }
                // nombre;nombre_cientifico;tipo;ciclo_dias;altitud_min;altitud_max;temp_min;temp_max;
                // precip_min_mm;precip_max_mm;tolerancia_inundacion;requiere_riego;grupos_cum;descripcion
                String[] c = linea.split(";", -1);

                Cultivo cultivo = new Cultivo();
                cultivo.setNombre(c[0].trim());
                cultivo.setNombreCientifico(c[1].trim());
                cultivo.setTipo(TipoCultivo.valueOf(c[2].trim()));
                cultivo.setCicloDias(c[3].isBlank() ? null : Integer.valueOf(c[3].trim()));
                cultivo.setDescripcion(c[13].trim());

                RequerimientoCultivo req = new RequerimientoCultivo();
                req.setAltitudMin(Integer.valueOf(c[4].trim()));
                req.setAltitudMax(Integer.valueOf(c[5].trim()));
                req.setTemperaturaMin(Double.valueOf(c[6].trim()));
                req.setTemperaturaMax(Double.valueOf(c[7].trim()));
                req.setPrecipitacionMinMm(Double.valueOf(c[8].trim()));
                req.setPrecipitacionMaxMm(Double.valueOf(c[9].trim()));
                req.setToleranciaInundacion(ToleranciaInundacion.valueOf(c[10].trim()));
                req.setRequiereRiego(Boolean.valueOf(c[11].trim()));
                cultivo.asignarRequerimiento(req);

                List<String> codigos = Arrays.stream(c[12].split("\\|")).map(String::trim).toList();
                Set<GrupoCum> grupos = new HashSet<>(grupoCumRepository.findByCodigoIn(codigos));
                cultivo.setGruposCum(grupos);

                cultivoRepository.save(cultivo);
                total++;
            }
        }
        log.info("Cultivos iniciales cargados: {}", total);
    }
}
