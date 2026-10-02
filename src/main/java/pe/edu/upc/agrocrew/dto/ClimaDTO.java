package pe.edu.upc.agrocrew.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClimaDTO {
    private Long predioId;
    /** Temperatura media de los últimos 12 meses (°C). */
    private Double temperaturaMedia;
    /** Lluvia acumulada en los últimos 12 meses (mm). */
    private Double precipitacionAnualMm;
    private List<DiaPronosticoDTO> pronostico;
    private LocalDateTime fechaConsulta;
    /** true si Open-Meteo no respondió y se muestran los últimos datos guardados. */
    private boolean datosGuardados;
    private String fuente;
}
