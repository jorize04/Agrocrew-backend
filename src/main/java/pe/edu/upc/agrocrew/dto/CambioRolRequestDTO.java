package pe.edu.upc.agrocrew.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CambioRolRequestDTO {

    @Schema(example = "ASESOR", allowableValues = {"PRODUCTOR", "ASESOR", "ADMIN"})
    @NotBlank(message = "El rol es obligatorio")
    @Pattern(regexp = "PRODUCTOR|ASESOR|ADMIN", message = "El rol debe ser PRODUCTOR, ASESOR o ADMIN")
    private String rol;
}
