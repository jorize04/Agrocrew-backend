package pe.edu.upc.agrocrew.models;

/** Factores que evalúa el motor de reglas y su peso máximo en el puntaje (suman 100). */
public enum TipoFactor {
    SUELO(30),
    ALTITUD(20),
    TEMPERATURA(20),
    AGUA(20),
    RIESGO_HIDRICO(10);

    private final int pesoMaximo;

    TipoFactor(int pesoMaximo) {
        this.pesoMaximo = pesoMaximo;
    }

    public int getPesoMaximo() {
        return pesoMaximo;
    }
}
