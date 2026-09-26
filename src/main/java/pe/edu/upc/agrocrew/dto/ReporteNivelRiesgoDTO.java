package pe.edu.upc.agrocrew.dto;

import pe.edu.upc.agrocrew.models.NivelRiesgo;

/** Cantidad por departamento y nivel de riesgo. */
public record ReporteNivelRiesgoDTO(String departamento, String nivelRiesgo, Long cantidad) {

    /** Constructor usado por las consultas JPQL, que devuelven el enum. */
    public ReporteNivelRiesgoDTO(String departamento, NivelRiesgo nivelRiesgo, Long cantidad) {
        this(departamento, nivelRiesgo != null ? nivelRiesgo.name() : null, cantidad);
    }
}
