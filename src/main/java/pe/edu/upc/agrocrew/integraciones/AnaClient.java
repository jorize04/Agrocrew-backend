package pe.edu.upc.agrocrew.integraciones;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.edu.upc.agrocrew.exceptions.IntegracionException;
import pe.edu.upc.agrocrew.models.NivelRiesgo;
import pe.edu.upc.agrocrew.models.ServicioExterno;

import java.util.*;

/**
 * Descarga los Puntos Crítico de riesgo hídrico publicados por la ANA
 * (servicio ArcGIS Público/PuntosCriticos, capa 125). En el log se muestran los campos
 * del primer registro para detectar si la ANA cambia la estructura de la capa.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnaClient {

    private static final int TAMANO_PAGINA = 1000;
    private static final int MAX_PAGINAS = 50;

    private final ClienteExterno clienteExterno;

    @Value("${app.integraciones.ana.capa-puntos-criticos}")
    private String capaPuntosCriticos;

    public record PuntoCriticoExterno(String codigo, String descripcion, String tipoPeligro,
                                      NivelRiesgo nivel, double latitud, double longitud, String ubicacion) {
    }

    public List<PuntoCriticoExterno> descargarPuntosCriticos() {
        List<PuntoCriticoExterno> puntos = new ArrayList<>();
        boolean paginar = true;

        for (int pagina = 0; pagina < MAX_PAGINAS; pagina++) {
            JsonNode respuesta;
            try {
                respuesta = consultar(paginar ? pagina * TAMANO_PAGINA : null);
            } catch (IntegracionException e) {
                if (pagina == 0 && paginar) {
                    // Algunas capas ArcGIS no aceptan paginación ("Invalid or missing input parameters").
                    // Se reintenta una sola vez sin paginar (trae hasta el máximo que permita el servicio).
                    log.warn("La capa de la ANA no aceptó paginación; se reintenta sin paginar");
                    paginar = false;
                    respuesta = consultar(null);
                } else {
                    throw e;
                }
            }
            JsonNode features = respuesta.path("features");
            for (JsonNode f : features) {
                PuntoCriticoExterno p = convertir(f, puntos.isEmpty());
                if (p != null) {
                    puntos.add(p);
                }
            }
            boolean hayMas = paginar && (respuesta.path("exceededTransferLimit").asBoolean(false)
                    || features.size() == TAMANO_PAGINA);
            if (!hayMas) {
                break;
            }
        }
        log.info("Puntos críticos descargados de la ANA: {}", puntos.size());
        return puntos;
    }

    private JsonNode consultar(Integer offset) {
        Map<String, String> p = new LinkedHashMap<>();
        p.put("where", "1=1");
        p.put("outFields", "*");
        p.put("returnGeometry", "true");
        p.put("outSR", "4326");
        if (offset != null) {
            p.put("resultOffset", String.valueOf(offset));
            p.put("resultRecordCount", String.valueOf(TAMANO_PAGINA));
        }
        p.put("f", "json");
        return clienteExterno.getJson(ServicioExterno.ANA,
                ClienteExterno.construirUri(capaPuntosCriticos + "/query", p));
    }

    /**
     * Convierte un registro de la capa "Puntos Críticos" de la ANA. Campos usados (verificados en la capa 125):
     * OBJECTID, Tipo, Rio_QDA, Sector, ACTIVIDAD, Peligro/TiposPeligro, Departamento/Provincia/Distrito,
     * Familias, Viviendas, CentroEducativo, CentroSalud.
     */
    private PuntoCriticoExterno convertir(JsonNode f, boolean mostrarCampos) {
        JsonNode a = f.path("attributes");
        if (mostrarCampos) {
            List<String> campos = new ArrayList<>();
            a.fieldNames().forEachRemaining(campos::add);
            log.info("Campos de Puntos Críticos ANA: {}", campos);
        }
        JsonNode g = f.path("geometry");
        if (!g.has("x") || !g.has("y") || !a.has("OBJECTID")) {
            return null; // solo se usan puntos con identificador
        }
        return new PuntoCriticoExterno(
                a.path("OBJECTID").asText(),
                recortar(descripcion(a), 500),
                recortar(tipoPeligro(a), 100),
                nivel(a),
                g.path("y").asDouble(),
                g.path("x").asDouble(),
                recortar(ubicacion(a), 200));
    }

    /** Ej.: "Río Huarmey, sector San Nicolas. Acción recomendada por la ANA: limpieza y descolmatación". */
    private String descripcion(JsonNode a) {
        StringBuilder sb = new StringBuilder();
        String tipo = valor(a, "Tipo");
        String rio = valor(a, "Rio_QDA");
        if (rio != null) {
            sb.append(tipo != null ? tipo + " " : "").append(rio);
        }
        String sector = valor(a, "Sector");
        if (sector != null) {
            sb.append(sb.isEmpty() ? "Sector " : ", sector ").append(sector);
        }
        String actividad = valor(a, "ACTIVIDAD");
        if (actividad != null) {
            sb.append(sb.isEmpty() ? "" : ". ").append("Acción recomendada por la ANA: ").append(actividad);
        }
        return sb.isEmpty() ? null : sb.toString();
    }

    /** La ANA suele publicar "Sin Especificar"; en ese caso se usa el peligro típico de estos puntos. */
    private String tipoPeligro(JsonNode a) {
        for (String campo : List.of("Peligro", "TiposPeligro", "tipos_peligro")) {
            String v = valor(a, campo);
            if (v != null && !v.toLowerCase().contains("sin especificar")) {
                return v;
            }
        }
        return "Inundación o desborde";
    }

    /**
     * La capa de la ANA no publica un nivel de riesgo. Criterio del proyecto: todo punto crítico es riesgo ALTO,
     * y MUY_ALTO si expone a 50 o más familias/viviendas o a un centro educativo o de salud.
     */
    private NivelRiesgo nivel(JsonNode a) {
        int familias = a.path("Familias").asInt(0);
        int viviendas = a.path("Viviendas").asInt(0);
        int educativos = a.path("CentroEducativo").asInt(0);
        int salud = a.path("CentroSalud").asInt(0);
        boolean muyExpuesto = familias >= 50 || viviendas >= 50 || educativos > 0 || salud > 0;
        return muyExpuesto ? NivelRiesgo.MUY_ALTO : NivelRiesgo.ALTO;
    }

    private String valor(JsonNode a, String campo) {
        JsonNode v = a.path(campo);
        return v.isMissingNode() || v.isNull() || v.asText().isBlank() ? null : v.asText().trim();
    }

    private String ubicacion(JsonNode a) {
        List<String> partes = new ArrayList<>();
        for (String campo : List.of("Departamento", "Provincia", "Distrito")) {
            String v = valor(a, campo);
            if (v != null) {
                partes.add(v);
            }
        }
        return partes.isEmpty() ? null : String.join(" / ", partes);
    }

    private String recortar(String s, int max) {
        return s == null || s.length() <= max ? s : s.substring(0, max);
    }
}
