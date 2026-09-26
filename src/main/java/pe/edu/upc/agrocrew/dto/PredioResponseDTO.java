package pe.edu.upc.agrocrew.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upc.agrocrew.models.FuenteAgua;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class PredioResponseDTO {
    private Long id;
    private String nombre;
    private String codigoCatastral;
    private Double latitud;
    private Double longitud;
    private Integer altitudMsnm;
    private BigDecimal areaHa;
    private FuenteAgua fuenteAgua;
    private Long distritoId;
    private String ubigeo;
    private String distrito;
    private String provincia;
    private String departamento;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
}
