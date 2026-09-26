package pe.edu.upc.agrocrew.models;

/** Nivel de riesgo hídrico, ordenado de menor a mayor. */
public enum NivelRiesgo {
    BAJO,
    MEDIO,
    ALTO,
    MUY_ALTO;

    public boolean esAltoOMayor() {
        return this == ALTO || this == MUY_ALTO;
    }

    /** Convierte el texto que viene de la ANA ("Muy Alto", "ALTA", "medio"...) a este enum. */
    public static NivelRiesgo desdeTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return MEDIO;
        }
        String t = texto.trim().toUpperCase()
                .replace("Á", "A").replace("É", "E").replace("Í", "I").replace("Ó", "O").replace("Ú", "U");
        if (t.contains("MUY")) {
            return MUY_ALTO;
        }
        if (t.startsWith("ALT")) {
            return ALTO;
        }
        if (t.startsWith("BAJ")) {
            return BAJO;
        }
        return MEDIO;
    }
}
