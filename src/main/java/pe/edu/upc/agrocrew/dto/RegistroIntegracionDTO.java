package pe.edu.upc.agrocrew.dto;

import pe.edu.upc.agrocrew.models.ServicioExterno;

import java.time.LocalDateTime;

public record RegistroIntegracionDTO(Long id, ServicioExterno servicio, String endpoint, Integer estadoHttp,
                                     Long duracionMs, Boolean exito, String mensajeError, LocalDateTime fecha) {
}
