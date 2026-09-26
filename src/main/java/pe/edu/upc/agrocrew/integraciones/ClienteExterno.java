package pe.edu.upc.agrocrew.integraciones;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import pe.edu.upc.agrocrew.exceptions.IntegracionException;
import pe.edu.upc.agrocrew.models.ServicioExterno;
import pe.edu.upc.agrocrew.services.RegistroIntegracionService;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Hace las llamadas GET a los servicios externos, mide el tiempo y deja registro
 * en la tabla registros_integracion (éxito o error). Todos los clientes lo usan.
 */
@Component
@RequiredArgsConstructor
public class ClienteExterno {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final RegistroIntegracionService registroIntegracionService;

    /** Arma la URL: base + "?" + parámetros codificados. La base puede venir ya codificada (ej. P%C3%BAblico). */
    public static URI construirUri(String base, Map<String, String> parametros) {
        String query = parametros.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        return URI.create(base + (base.contains("?") ? "&" : "?") + query);
    }

    public JsonNode getJson(ServicioExterno servicio, URI uri) {
        long inicio = System.currentTimeMillis();
        String endpoint = uri.toString();
        try {
            String cuerpo = restClient.get().uri(uri).retrieve().body(String.class);
            JsonNode json = objectMapper.readTree(cuerpo == null ? "{}" : cuerpo);

            // Los servicios ArcGIS responden 200 pero con {"error": {...}} cuando algo falla.
            if (json.has("error")) {
                String detalle = json.path("error").path("message").asText(json.path("error").toString());
                registroIntegracionService.registrar(servicio, endpoint, 200, duracion(inicio), false, detalle);
                throw new IntegracionException("El servicio " + servicio + " respondió con error: " + detalle);
            }
            registroIntegracionService.registrar(servicio, endpoint, 200, duracion(inicio), true, null);
            return json;

        } catch (IntegracionException e) {
            throw e;
        } catch (RestClientResponseException e) {
            registroIntegracionService.registrar(servicio, endpoint, e.getStatusCode().value(), duracion(inicio),
                    false, e.getMessage());
            throw new IntegracionException("El servicio " + servicio + " no está disponible (HTTP "
                    + e.getStatusCode().value() + ")");
        } catch (Exception e) {
            registroIntegracionService.registrar(servicio, endpoint, null, duracion(inicio), false,
                    e.getClass().getSimpleName() + ": " + e.getMessage());
            throw new IntegracionException("El servicio " + servicio + " no está disponible temporalmente");
        }
    }

    private long duracion(long inicio) {
        return System.currentTimeMillis() - inicio;
    }
}
