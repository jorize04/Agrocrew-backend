package pe.edu.upc.agrocrew.dto;

public record ReporteCultivoDTO(String cultivo, Long vecesRecomendado, Double puntajePromedio) {

    /** Redondea el promedio a un decimal. */
    public ReporteCultivoDTO {
        if (puntajePromedio != null) {
            puntajePromedio = Math.round(puntajePromedio * 10) / 10.0;
        }
    }
}
