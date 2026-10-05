package pe.edu.upc.agrocrew.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upc.agrocrew.models.NivelRiesgo;
import pe.edu.upc.agrocrew.models.TipoAlerta;

import java.time.LocalDateTime;

/**
 * Alerta de riesgo hídrico o de lluvia intensa que se muestra al productor.
 */
@Data
@NoArgsConstructor
public class AlertaDTO {
    /** Identificador de la alerta. */
    private Long id;
    /** Identificador del predio al que corresponde la alerta. */
    private Long predioId;
    /** Nombre del predio. */
    private String predio;
    /** Tipo de alerta. */
    private TipoAlerta tipo;
    /** Nivel de riesgo de la alerta. */
    private NivelRiesgo nivel;
    /** Título breve de la alerta. */
    private String titulo;
    /** Mensaje explicativo para el productor. */
    private String mensaje;
    /** Acciones que se recomienda tomar. */
    private String accionesSugeridas;
    /** Fecha y hora en que se generó la alerta. */
    private LocalDateTime fechaGeneracion;
    /** Indica si el usuario ya la leyó. */
    private Boolean leida;
    /** Fecha y hora en que se marcó como leída. */
    private LocalDateTime fechaLectura;
}
