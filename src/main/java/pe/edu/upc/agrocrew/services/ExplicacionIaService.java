package pe.edu.upc.agrocrew.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import pe.edu.upc.agrocrew.exceptions.IntegracionException;
import pe.edu.upc.agrocrew.integraciones.GeminiClient;
import pe.edu.upc.agrocrew.models.GrupoCum;
import pe.edu.upc.agrocrew.models.Predio;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Componente de inteligencia artificial de AgroCrew.
 *
 * Problema que resuelve: el resultado del motor de reglas (puntajes y factores) es técnico; la IA lo
 * convierte en una explicación breve y en lenguaje sencillo para el productor.
 * Modelo: Google Gemini (por defecto gemini-2.5-flash, configurable).
 * Datos que usa: solo los datos de la evaluación (suelo, altitud, clima, riesgo y ranking). No se envían
 * datos personales del usuario.
 * Límite: la IA solo redacta; nunca decide ni modifica el ranking. Si falla, se usa la plantilla.
 * Idioma: responde en inglés si la petición llega con Accept-Language "en"; en otro caso, en español.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExplicacionIaService {

    private final GeminiClient geminiClient;

    public record Resultado(String texto, String modelo) {
    }

    public Optional<Resultado> generar(Predio predio, GrupoCum grupo, MotorReglasService.DatosPredio d,
                                       List<MotorReglasService.ResultadoCultivo> ranking,
                                       MotorReglasService.ResultadoCultivo consultado) {
        if (!geminiClient.habilitado()) {
            return Optional.empty();
        }
        try {
            String texto = geminiClient.generarTexto(construirPrompt(predio, grupo, d, ranking, consultado));
            return Optional.of(new Resultado(texto, geminiClient.getModelo()));
        } catch (IntegracionException e) {
            log.warn("La IA no respondió; se usará la explicación con plantilla: {}", e.getMessage());
            return Optional.empty();
        }
    }

    String construirPrompt(Predio predio, GrupoCum grupo, MotorReglasService.DatosPredio d,
                           List<MotorReglasService.ResultadoCultivo> ranking,
                           MotorReglasService.ResultadoCultivo consultado) {
        boolean ingles = "en".equalsIgnoreCase(LocaleContextHolder.getLocale().getLanguage());
        String cultivos = ranking.stream().limit(5)
                .map(r -> String.format(Locale.US, "%s (%.0f/100, %s)", r.cultivo().getNombre(), r.puntaje(),
                        r.compatibilidad()))
                .collect(Collectors.joining("; "));
        String limitantes = ranking.stream().limit(3)
                .flatMap(r -> r.factores().stream()
                        .filter(f -> f.efecto().name().equals("LIMITANTE"))
                        .map(f -> r.cultivo().getNombre() + ": " + f.detalle()))
                .collect(Collectors.joining("; "));

        StringBuilder p = new StringBuilder();
        p.append(ingles
                ? "You are an agricultural assistant for small farmers in Peru. Write in simple English, maximum 120 words, in one or two short paragraphs, without lists or markdown.\n"
                : "Eres un asistente agrícola para pequeños productores del Perú. Escribe en español sencillo, máximo 120 palabras, en uno o dos párrafos cortos, sin listas ni formato markdown.\n");
        p.append(ingles
                ? "Explain the result below. Use ONLY these data; do not invent figures, do not add or reorder crops, do not promise yields. End by reminding that the recommendation is a reference and does not replace an agronomist.\n\n"
                : "Explica el resultado de abajo. Usa SOLO estos datos; no inventes cifras, no agregues ni reordenes cultivos y no prometas rendimientos. Termina recordando que la recomendación es referencial y no reemplaza a un ingeniero agrónomo.\n\n");
        p.append("Predio: ").append(predio.getNombre()).append('\n');
        p.append("Suelo (Capacidad de Uso Mayor): ")
                .append(grupo != null ? grupo.getCodigo() + " - " + grupo.getNombre() : "sin clasificación oficial").append('\n');
        p.append("Altitud: ").append(d.altitudMsnm() != null ? d.altitudMsnm() + " msnm" : "sin dato").append('\n');
        p.append("Temperatura media anual: ").append(d.temperaturaMedia() != null ? d.temperaturaMedia() + " °C" : "sin dato").append('\n');
        p.append("Lluvia anual: ").append(d.precipitacionAnualMm() != null ? d.precipitacionAnualMm() + " mm" : "sin dato").append('\n');
        p.append("Fuente de agua: ").append(d.fuenteAgua()).append('\n');
        p.append("Riesgo de inundación (ANA): ").append(d.nivelRiesgo()).append('\n');
        p.append("Cultivos compatibles en orden: ").append(cultivos.isEmpty() ? "ninguno" : cultivos).append('\n');
        if (!limitantes.isEmpty()) {
            p.append("Factores limitantes: ").append(limitantes).append('\n');
        }
        if (consultado != null) {
            p.append(String.format(Locale.US, "Cultivo consultado por el productor: %s, compatibilidad %s (%.0f/100)%n",
                    consultado.cultivo().getNombre(), consultado.compatibilidad(), consultado.puntaje()));
        }
        return p.toString();
    }
}
