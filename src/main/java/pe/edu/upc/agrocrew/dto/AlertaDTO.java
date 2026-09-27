package pe.edu.upc.agrocrew.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upc.agrocrew.models.NivelRiesgo;
import pe.edu.upc.agrocrew.models.TipoAlerta;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class AlertaDTO {
    private Long ;
    private Long predioId;
    private String predio;
    private TipoAlerta tipo;
    private NivelRiesgo nivel;
    private String titulo;
    private String mensaje;
    private String accionesSugeridas;
    private LocalDateTime fechaGeneracion;
    private Boolean leida;
    private LocalDateTime fechaLectura;
}
