package pe.edu.upc.agrocrew.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upc.agrocrew.models.EstadoEvaluacion;
import pe.edu.upc.agrocrew.models.FuenteAgua;
import pe.edu.upc.agrocrew.models.NivelRiesgo;
import pe.edu.upc.agrocrew.models.TipoEvaluacion;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class EvaluacionResponseDTO {
    private Long id;
    private Long predioId;
    private String predio;
    private LocalDateTime fecha;
    private TipoEvaluacion tipo;
    private EstadoEvaluacion estado;

    // Datos usados
    private String grupoCum;
    private String grupoCumNombre;
    private String cumCodigoOriginal;
    private String calidadAgrologica;
    private String limitacionesSuelo;
    private Integer altitudMsnm;
    private Double temperaturaMedia;
    private Double precipitacionAnualMm;
    private FuenteAgua fuenteAgua;
    private NivelRiesgo nivelRiesgoHidrico;
    private Double distanciaPuntoCriticoKm;
    private List<String> fuentesFaltantes;

    private String explicacion;
    private boolean explicacionPorIa;
    private String modeloIa;
    private String aviso;

    /** Ranking de cultivos compatibles (compatibilidad ALTA o MEDIA). Vacío si el terreno no es apto. */
    private List<RecomendacionDTO> ranking;

    /** Solo en evaluaciones de un cultivo específico. */
    private RecomendacionDTO resultadoCultivoConsultado;
}
