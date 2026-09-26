package pe.edu.upc.agrocrew.integraciones;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.edu.upc.agrocrew.exceptions.IntegracionException;
import pe.edu.upc.agrocrew.models.ServicioExterno;
import pe.edu.upc.agrocrew.util.CumParser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Consulta la Capacidad de Uso Mayor (CUM) en los geoservicios ArcGIS del Estado.
 * Se configuran una o varias capas en app.integraciones.midagri.capas-cum (separadas por coma);
 * se prueba cada una en orden hasta encontrar la clasificación del punto.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MidagriClient {

    private final ClienteExterno clienteExterno;
    private final ObjectMapper objectMapper;

    @Value("${app.integraciones.midagri.capas-cum}")
    private List<String> capasCum;

    /**
     * @return la clasificación, o vacío si los servicios respondieron pero no hay dato para ese punto.
     * @throws IntegracionException si ninguna capa pudo consultarse.
     */
    public Optional<CumParser.Cum> consultarCum(double latitud, double longitud) {
        int capasConError = 0;
        for (String capa : capasCum) {
            Map<String, String> p = new LinkedHashMap<>();
            p.put("geometry", longitud + "," + latitud);
            p.put("geometryType", "esriGeometryPoint");
            p.put("inSR", "4326");
            p.put("spatialRel", "esriSpatialRelIntersects");
            p.put("outFields", "*");
            p.put("returnGeometry", "false");
            p.put("f", "json");
            try {
                JsonNode respuesta = clienteExterno.getJson(ServicioExterno.MIDAGRI,
                        ClienteExterno.construirUri(capa.trim() + "/query", p));
                for (JsonNode feature : respuesta.path("features")) {
                    Map<String, Object> atributos = objectMapper.convertValue(
                            feature.path("attributes"), new TypeReference<>() { });
                    Optional<CumParser.Cum> cum = CumParser.desdeAtributos(atributos);
                    if (cum.isPresent()) {
                        return cum;
                    }
                    // Ayuda a ajustar el parser si la capa usa otros nombres de campo.
                    log.warn("MIDAGRI devolvió un polígono pero no se reconoció la CUM. Atributos: {}", atributos);
                }
            } catch (IntegracionException e) {
                capasConError++;
            }
        }
        if (capasConError == capasCum.size()) {
            throw new IntegracionException("No se pudo consultar la clasificación de suelos del MIDAGRI");
        }
        return Optional.empty();
    }
}
