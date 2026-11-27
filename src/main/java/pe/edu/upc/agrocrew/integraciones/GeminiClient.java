package pe.edu.upc.agrocrew.integraciones;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.edu.upc.agrocrew.exceptions.IntegracionException;
import pe.edu.upc.agrocrew.models.ServicioExterno;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Cliente del modelo de lenguaje Google Gemini (API generateContent de Google AI Studio).
 * La API key se lee de la variable de entorno IA_API_KEY y viaja en un header, nunca en la URL.
 * Si no hay API key configurada, la IA queda desactivada y se usa la explicación con plantilla.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiClient {

    private final ClienteExterno clienteExterno;

    @Value("${app.ia.api-key:${IA_API_KEY:}}")
    private String apiKey;

    @Value("${app.ia.modelo:${IA_MODELO:gemini-3.5-flash-lite}}")
    private String modelo;

    /** Modelo que se intenta si el principal falla (saturado, retirado, etc.). Vacío = sin respaldo. */
    @Value("${app.ia.modelo-respaldo:${IA_MODELO_RESPALDO:gemini-3.1-flash-lite}}")
    private String modeloRespaldo;

    @Value("${app.ia.url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String urlBase;

    /** Deja constancia en el log, al arrancar, de si la IA quedó activa (sin mostrar nunca la clave). */
    @PostConstruct
    void informarEstado() {
        if (habilitado()) {
            log.info("IA activada: modelo {} (respaldo: {})", modelo,
                    modeloRespaldo == null || modeloRespaldo.isBlank() ? "ninguno" : modeloRespaldo);
        } else {
            log.warn("IA desactivada: falta IA_API_KEY. Las explicaciones se generarán con plantilla.");
        }
    }

    public boolean habilitado() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String getModelo() {
        return modelo;
    }

    public String getModeloRespaldo() {
        return modeloRespaldo;
    }

    /** Envía el prompt al modelo principal y devuelve el texto generado. */
    public String generarTexto(String prompt) {
        return generarTexto(prompt, modelo);
    }

    /** Envía el prompt al modelo indicado y devuelve el texto generado. */
    public String generarTexto(String prompt, String modeloAUsar) {
        URI uri = URI.create(urlBase + "/" + modeloAUsar + ":generateContent");
        Map<String, Object> cuerpo = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("temperature", 0.3, "maxOutputTokens", 2048));

        JsonNode respuesta = clienteExterno.postJson(ServicioExterno.IA, uri,
                Map.of("x-goog-api-key", apiKey), cuerpo);

        StringBuilder texto = new StringBuilder();
        for (JsonNode parte : respuesta.path("candidates").path(0).path("content").path("parts")) {
            texto.append(parte.path("text").asText(""));
        }
        if (texto.isEmpty()) {
            throw new IntegracionException("El modelo de IA no devolvió texto");
        }
        return texto.toString().trim();
    }
}
