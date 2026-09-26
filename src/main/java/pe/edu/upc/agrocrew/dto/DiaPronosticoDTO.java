package pe.edu.upc.agrocrew.dto;

import java.time.LocalDate;

public record DiaPronosticoDTO(LocalDate fecha, Double temperaturaMax, Double temperaturaMin, Double precipitacionMm) {
}
