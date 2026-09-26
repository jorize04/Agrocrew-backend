package pe.edu.upc.agrocrew.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interpreta la clasificación de Capacidad de Uso Mayor (CUM) que devuelve MIDAGRI.
 * Acepta el símbolo técnico (ej. "A2s", "C3se-P2sec") o el texto (ej. "Tierras aptas para pastos").
 * En asociaciones como "A2s-C3se" se toma la primera, que es la dominante.
 */
public final class CumParser {

    /** Resultado ya interpretado. calidad y limitaciones pueden ser nulas si solo había texto. */
    public record Cum(String codigoOriginal, String grupo, String calidad, String limitaciones) {
    }

    private static final Pattern SIMBOLO = Pattern.compile("^\\s*([ACPFX])\\s*([123])?\\s*([a-z]{0,6})", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLAVE_PRIORITARIA = Pattern.compile("CUM|CAP|SIMB|USO|CLAS|GRUPO", Pattern.CASE_INSENSITIVE);

    private CumParser() {
    }

    public static Optional<Cum> desdeAtributos(Map<String, Object> atributos) {
        // 1) Campos con nombre sugerente. Primero el símbolo (SIMCUM, trae calidad y limitaciones),
        //    luego el resto (GRUPOCUM, DESCUM, CAP_USO...), probando símbolo o texto.
        List<Map.Entry<String, Object>> prioritarios = new ArrayList<>();
        for (Map.Entry<String, Object> e : atributos.entrySet()) {
            if (e.getValue() instanceof String && CLAVE_PRIORITARIA.matcher(e.getKey()).find()) {
                prioritarios.add(e);
            }
        }
        prioritarios.sort((a, b) -> Boolean.compare(!a.getKey().toUpperCase().contains("SIM"),
                !b.getKey().toUpperCase().contains("SIM")));
        for (Map.Entry<String, Object> e : prioritarios) {
            String valor = (String) e.getValue();
            Optional<Cum> cum = desdeSimbolo(valor).or(() -> desdeTexto(valor));
            if (cum.isPresent()) {
                return cum;
            }
        }
        // 2) Cualquier otro campo, solo por texto descriptivo (evita confundir un "A" suelto).
        for (Object v : atributos.values()) {
            if (v instanceof String valor && valor.length() > 12) {
                Optional<Cum> cum = desdeTexto(valor);
                if (cum.isPresent()) {
                    return cum;
                }
            }
        }
        return Optional.empty();
    }

    public static Optional<Cum> desdeSimbolo(String valor) {
        if (valor == null || valor.isBlank() || valor.trim().length() > 30) {
            return Optional.empty();
        }
        // Se toma la primera clasificación de una asociación ("A2s-C3se") y se quitan las
        // anotaciones entre paréntesis, como "(r)" = requiere riego ("C3s(r)" -> "C3s").
        String primero = valor.trim().split("[-/]")[0].replaceAll("\\(.*?\\)", "").trim();
        Matcher m = SIMBOLO.matcher(primero);
        if (!m.find() || !m.group(0).trim().equals(primero.trim())) {
            return Optional.empty();
        }
        String grupo = m.group(1).toUpperCase();
        String calidad = switch (m.group(2) == null ? "" : m.group(2)) {
            case "1" -> "ALTA";
            case "2" -> "MEDIA";
            case "3" -> "BAJA";
            default -> null;
        };
        String limitaciones = traducirLimitaciones(m.group(3));
        return Optional.of(new Cum(valor.trim(), grupo, calidad, limitaciones));
    }

    public static Optional<Cum> desdeTexto(String valor) {
        if (valor == null) {
            return Optional.empty();
        }
        String t = valor.toLowerCase()
                .replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u");
        String grupo = null;
        if (t.contains("limpio")) {
            grupo = "A";
        } else if (t.contains("permanente")) {
            grupo = "C";
        } else if (t.contains("pasto")) {
            grupo = "P";
        } else if (t.contains("forestal")) {
            grupo = "F";
        } else if (t.contains("proteccion")) {
            grupo = "X";
        }
        return grupo == null ? Optional.empty() : Optional.of(new Cum(valor.trim(), grupo, null, null));
    }

    /** s = suelo, e = erosión, c = clima, w = drenaje, i = inundación, l = salinidad. */
    static String traducirLimitaciones(String letras) {
        if (letras == null || letras.isBlank()) {
            return null;
        }
        List<String> lista = new ArrayList<>();
        for (char c : letras.toLowerCase().toCharArray()) {
            String nombre = switch (c) {
                case 's' -> "suelo";
                case 'e' -> "erosión";
                case 'c' -> "clima";
                case 'w' -> "drenaje";
                case 'i' -> "inundación";
                case 'l' -> "salinidad";
                default -> null;
            };
            if (nombre != null && !lista.contains(nombre)) {
                lista.add(nombre);
            }
        }
        return lista.isEmpty() ? null : String.join(", ", lista);
    }
}
