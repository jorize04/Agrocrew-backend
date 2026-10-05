package pe.edu.upc.agrocrew.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upc.agrocrew.models.FuenteAgua;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Datos de un predio (terreno) registrado por el productor.
 */
@Data
@NoArgsConstructor
public class PredioResponseDTO {
    /** Identificador del predio. */
    private Long id;
    /** Nombre del predio. */
    private String nombre;
    /** Código catastral del predio. */
    private String codigoCatastral;
    /** Latitud de la ubicación del predio. */
    private Double latitud;
    /** Longitud de la ubicación del predio. */
    private Double longitud;
    /** Altitud en metros sobre el nivel del mar (msnm). */
    private Integer altitudMsnm;
    /** Área del predio en hectáreas. */
    private BigDecimal areaHa;
    /** Fuente de agua del predio. */
    private FuenteAgua fuenteAgua;
    /** Identificador del distrito donde se ubica. */
    private Long distritoId;
    /** Código de ubigeo del distrito. */
    private String ubigeo;
    /** Nombre del distrito. */
    private String distrito;
    /** Nombre de la provincia. */
    private String provincia;
    /** Nombre del departamento. */
    private String departamento;
    /** Fecha y hora de registro del predio. */
    private LocalDateTime fechaRegistro;
    /** Fecha y hora de la última actualización. */
    private LocalDateTime fechaActualizacion;
}
