package pe.edu.upc.agrocrew.integraciones;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.edu.upc.agrocrew.exceptions.IntegracionException;
import pe.edu.upc.agrocrew.models.ServicioExterno;

import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Consulta el Catastro Rural del MIDAGRI (US07) en el servicio ArcGIS del visor OCAPAS del SERFOR.
 * El servicio publica una capa "Predios Rurales - [Departamento]" por departamento; cada predio tiene
 * su código catastral (UNICAT), nombre (NOMPRE), ubigeo del distrito (UBIDIS) y área en hectáreas.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CatastroClient {

    private static final String PREFIJO_CAPA = "PREDIOS RURALES - ";

    private final ClienteExterno clienteExterno;

    @Value("${app.integraciones.catastro.servicio-url}")
    private String servicioUrl;

    /** Departamento normalizado (JUNIN) -> id de su capa en el servicio. Se carga una sola vez. */
    private final Map<String, Integer> capasPorDepartamento = new ConcurrentHashMap<>();

    /** Predio leído del catastro. */
    public record PredioCatastral(String codigo, String nombre, String departamento, String provincia,
                                  String distrito, String ubigeo, Double latitud, Double longitud, Double areaHa) {
    }

    /**
     * @param departamento nombre del departamento (como en el ubigeo del INEI, por ejemplo "JUNIN")
     * @param codigo       código catastral (UNICAT), ya validado
     * @return el predio, o vacío si el código no existe en el catastro de ese departamento
     * @throws IntegracionException si el servicio del MIDAGRI no responde
     */
    public Optional<PredioCatastral> buscarPorCodigo(String departamento, String codigo) {
        Integer capa = capaDe(departamento);
        if (capa == null) {
            log.warn("El catastro no publica una capa para el departamento {}", departamento);
            return Optional.empty();
        }
        Map<String, String> p = new LinkedHashMap<>();
        p.put("where", "UNICAT='" + codigo + "'");
        p.put("outFields", "*");
        p.put("returnGeometry", "true");
        p.put("outSR", "4326");
        p.put("geometryPrecision", "6");
        p.put("resultRecordCount", "1");
        p.put("f", "json");
        JsonNode respuesta = clienteExterno.getJson(ServicioExterno.MIDAGRI,
                ClienteExterno.construirUri(servicioUrl + "/" + capa + "/query", p));
        JsonNode features = respuesta.path("features");
        if (!features.isArray() || features.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(leerPredio(features.get(0)));
    }

    /** Convierte un "feature" de ArcGIS en un predio (público para las pruebas unitarias). */
    public static PredioCatastral leerPredio(JsonNode feature) {
        JsonNode a = feature.path("attributes");
        double[] centro = centroide(feature.path("geometry").path("rings"));
        return new PredioCatastral(
                texto(a, "UNICAT"), texto(a, "NOMPRE"), texto(a, "DEPAR"), texto(a, "PROVI"),
                texto(a, "DISTR"), texto(a, "UBIDIS"),
                centro == null ? null : redondear(centro[1], 6),
                centro == null ? null : redondear(centro[0], 6),
                area(a));
    }

    /**
     * Centro del polígono (centroide por la fórmula del área, sobre el anillo exterior).
     * Devuelve {longitud, latitud}, o null si no hay geometría.
     */
    static double[] centroide(JsonNode rings) {
        if (!rings.isArray() || rings.isEmpty() || !rings.get(0).isArray() || rings.get(0).isEmpty()) {
            return null;
        }
        JsonNode anillo = rings.get(0);
        double areaDoble = 0, cx = 0, cy = 0, sumaX = 0, sumaY = 0;
        int n = anillo.size();
        for (int i = 0; i < n; i++) {
            double x0 = anillo.get(i).get(0).asDouble();
            double y0 = anillo.get(i).get(1).asDouble();
            double x1 = anillo.get((i + 1) % n).get(0).asDouble();
            double y1 = anillo.get((i + 1) % n).get(1).asDouble();
            double cruz = x0 * y1 - x1 * y0;
            areaDoble += cruz;
            cx += (x0 + x1) * cruz;
            cy += (y0 + y1) * cruz;
            sumaX += x0;
            sumaY += y0;
        }
        if (Math.abs(areaDoble) < 1e-15) {
            return new double[]{sumaX / n, sumaY / n};
        }
        return new double[]{cx / (3 * areaDoble), cy / (3 * areaDoble)};
    }

    /** El campo de área cambia de nombre en cada capa (ej. SERFOR_IDE.USUIDE.PRED_JUNIN.AREA). */
    private static Double area(JsonNode atributos) {
        Iterator<Map.Entry<String, JsonNode>> campos = atributos.fields();
        while (campos.hasNext()) {
            Map.Entry<String, JsonNode> campo = campos.next();
            String nombre = campo.getKey().toUpperCase(Locale.ROOT);
            if ((nombre.equals("AREA") || nombre.endsWith(".AREA")) && campo.getValue().isNumber()) {
                return redondear(campo.getValue().asDouble(), 4);
            }
        }
        return null;
    }

    private Integer capaDe(String departamento) {
        if (capasPorDepartamento.isEmpty()) {
            cargarCapas();
        }
        return capasPorDepartamento.get(normalizar(departamento));
    }

    private synchronized void cargarCapas() {
        if (!capasPorDepartamento.isEmpty()) {
            return;
        }
        JsonNode servicio = clienteExterno.getJson(ServicioExterno.MIDAGRI,
                ClienteExterno.construirUri(servicioUrl, Map.of("f", "json")));
        for (JsonNode capa : servicio.path("layers")) {
            String nombre = normalizar(capa.path("name").asText());
            if (nombre.startsWith(PREFIJO_CAPA)) {
                capasPorDepartamento.put(nombre.substring(PREFIJO_CAPA.length()).trim(), capa.path("id").asInt());
            }
        }
        log.info("Capas del Catastro Rural cargadas: {}", capasPorDepartamento.size());
    }

    /** "Junín" -> "JUNIN": sin tildes y en mayúsculas, igual que el ubigeo del INEI. */
    static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toUpperCase(Locale.ROOT).trim();
    }

    private static String texto(JsonNode atributos, String campo) {
        JsonNode valor = atributos.path(campo);
        return valor.isMissingNode() || valor.isNull() ? null : valor.asText().trim();
    }

    private static double redondear(double valor, int decimales) {
        double factor = Math.pow(10, decimales);
        return Math.round(valor * factor) / factor;
    }
}
